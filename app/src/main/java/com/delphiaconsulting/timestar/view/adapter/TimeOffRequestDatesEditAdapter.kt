package com.delphiaconsulting.timestar.view.adapter

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.core.content.ContextCompat
import androidx.collection.LongSparseArray
import androidx.recyclerview.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Pair
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants.*
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder
import com.delphiaconsulting.timestar.App
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.action.creators.TimeOffActionsCreator
import com.delphiaconsulting.timestar.event.OnDateBalancesServiceError
import com.delphiaconsulting.timestar.event.OnNewDateBalancesAvailable
import com.delphiaconsulting.timestar.event.OnTimeOffBalancesReceived
import com.delphiaconsulting.timestar.event.OnTimeOffDateItemRemoved
import com.delphiaconsulting.timestar.net.analytics.AnalyticsCategories
import com.delphiaconsulting.timestar.net.analytics.FirebaseEvents
import com.delphiaconsulting.timestar.net.analytics.Tracker
import com.delphiaconsulting.timestar.net.gson.*
import com.delphiaconsulting.timestar.store.TimeOffStore
import com.delphiaconsulting.timestar.util.Preferences
import com.delphiaconsulting.timestar.util.StringUtil
import com.delphiaconsulting.timestar.view.extension.onItemSelected
import com.delphiaconsulting.timestar.view.fragment.TimePickerDialogFragment
import kotlinx.android.synthetic.main.time_off_edit_date_item.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by qktran on 1/22/17.
 */
open class TimeOffRequestDatesEditAdapter internal constructor(protected var context: Context, val fragmentManager: FragmentManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), SwipeableItemAdapter<TimeOffRequestDatesEditAdapter.TimeOffDateHolder> {

    companion object {
        private const val DATE_TEXT_PICKER_TAG = "DATE_TEXT_PICKER_TAG"
        private const val SCHEDULE_SPINNER_TIME_PICKER_TAG = "SCHEDULE_SPINNER_TIME_PICKER_TAG"
        private const val BALANCE_TYPE_NOT_EQUAL_THREE_MAP_KEY = "BALANCE_TYPE_NOT_EQUAL_THREE_MAP_KEY"
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var actionsCreator: TimeOffActionsCreator
    @Inject lateinit var store: TimeOffStore
    @Inject lateinit var tracker: Tracker

    protected var requestDates = ArrayList<TimeOffRequestDate>()
    private lateinit var balancesMeta: TimeOffBalancesMeta
    private lateinit var balances: TimeOffBalances
    protected lateinit var requestMeta: TimeOffRequestsMeta

    private var runningBalances = LongSparseArray<Double>()
    private var lastBalancesByBucket: SparseArray<Map<String, Double>> = SparseArray()

    init {
        this.setHasStableIds(true)
    }

    fun setRequestDates(requestDates: List<TimeOffRequestDate>) {
        this.requestDates.addAll(requestDates)
        if (!isBalanceTypeThreeUser) {
            calculateRunningBalances()
            return
        }
        this.requestDates.sortBy { it }
        requestBalanceTypeThree()
    }

    private val isBalanceTypeThreeUser: Boolean
        get() = balancesMeta.balanceType == 3

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        (context as App).component.inject(this)
        super.onAttachedToRecyclerView(recyclerView)
        bus.register(this)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        bus.unregister(this)
        super.onDetachedFromRecyclerView(recyclerView)
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeOffBalancesReceived) {
        this.balancesMeta = event.timeOffBalancesMeta
        this.balances = event.timeOffBalances
        this.requestMeta = event.timeOffRequestsMeta
    }

    override fun getItemId(position: Int) = requestDates[position].id

    override fun getItemCount() = requestDates.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = TimeOffDateHolder(LayoutInflater.from(parent.context).inflate(R.layout.time_off_edit_date_item, parent, false))

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val requestDate = requestDates[position]
        val holder = viewHolder as TimeOffDateHolder
        holder.itemView.dateText.text = requestDate.formattedDate
        holder.itemView.dateText.setOnClickListener {
            val datetime = requestDate.dateTime
            CalendarDatePickerDialogFragment()
                    .setFirstDayOfWeek(Calendar.SUNDAY)
                    .setThemeCustom(R.style.GreenBetterPickers)
                    .setPreselectedDate(datetime.year, datetime.monthOfYear - 1, datetime.dayOfMonth)
                    .setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        requestDate.setDate(DateTime().withDayOfMonth(dayOfMonth).withMonthOfYear(monthOfYear + 1).withYear(year).toDate())
                        holder.itemView.dateText.text = requestDate.formattedDate
                        if (isBalanceTypeThreeUser) {
                            requestBalanceTypeThree()
                        }
                        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Edit", "date")
                        tracker.trackFirebaseEvent(FirebaseEvents.EDIT_ITEM, ITEM_NAME, "date")
                    }
                    .show(fragmentManager, DATE_TEXT_PICKER_TAG)
        }

        holder.itemView.hoursText.setText(getHoursText(requestDate.id, StringUtil.hoursAndMinutes(requestDate.minutes)), TextView.BufferType.SPANNABLE)
        holder.itemView.hoursText.setOnClickListener { _ ->
            TimePickerDialogFragment.newInstance(incrementMinutes = requestMeta.incrementMinutes, preselectedMinutes = requestDate.minutes)
                    .setOnTimeValueSetListener {
                        requestDate.minutes = it
                        holder.itemView.hoursText.text = StringUtil.hoursAndMinutes(requestDate.minutes)
                        calculateRunningBalances()
                        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Edit", "hours")
                        tracker.trackFirebaseEvent(FirebaseEvents.EDIT_ITEM, ITEM_NAME, "hours")
                    }
                    .show(fragmentManager, TimePickerDialogFragment.TAG)
        }
        holder.swipeItemHorizontalSlideAmount = 0f

        onBindPayTypeView(holder, requestDate)
        onBindSchedulingView(holder, requestDate)
    }

    private fun getHoursText(itemId: Long, baseHoursText: String): CharSequence {
        val balance = runningBalances.get(itemId) ?: return baseHoursText
        val hoursText = String.format(Locale.US, "%s (%" + (if (balance % 1 == 0.0) ".0" else ".2") + "f)", baseHoursText, balance)
        val spannable = SpannableString(hoursText)
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, if (balance < 0) R.color.insperity_red else R.color.insperity_green)), hoursText.indexOf("(") + 1, hoursText.indexOf(")"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun onBindPayTypeView(holder: TimeOffDateHolder, requestDate: TimeOffRequestDate) {
        val adapter = ArrayAdapter(context, R.layout.spinner_textview, requestMeta.payTypesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.itemView.payTypeSpinner.adapter = adapter
        holder.itemView.payTypeSpinner.onItemSelectedListener = null
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i)?.value == requestDate.payType) {
                holder.itemView.payTypeSpinner.setSelection(i, true)
                break
            }
        }
        var initializingSpinner = true
        holder.itemView.payTypeSpinner.onItemSelected { adapterView, _ ->
            if (initializingSpinner) {
                initializingSpinner = false
                return@onItemSelected
            }
            requestDate.payType = (adapterView.selectedItem as ITAReferenceAttr).value
            calculateRunningBalances()
            tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Edit", "type")
            tracker.trackFirebaseEvent(FirebaseEvents.EDIT_ITEM, ITEM_NAME, "type")
        }
    }

    private fun onBindSchedulingView(holder: TimeOffDateHolder, requestDate: TimeOffRequestDate) {
        if (!requestMeta.canSchedule) {
            holder.itemView.schedulingContainer.visibility = View.GONE
            return
        }
        val adapter = ArrayAdapter(context, R.layout.spinner_textview, requestMeta.schedulingList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.itemView.schedulingSpinner.adapter = adapter
        holder.itemView.schedulingSpinner.onItemSelectedListener = null
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i)?.value == requestDate.scheduling.toString()) {
                holder.itemView.schedulingSpinner.setSelection(i, true)
                break
            }
        }
        if (requestDate.scheduling != 0) {
            holder.itemView.startTimeText.visibility = View.VISIBLE
            holder.itemView.startTimeText.text = requestDate.startTime
        }
        holder.itemView.schedulingSpinner.onItemSelected { adapterView, position ->
            tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Edit", "schedule")
            tracker.trackFirebaseEvent(FirebaseEvents.EDIT_ITEM, ITEM_NAME, "schedule")
            requestDate.scheduling = adapterView.selectedItemPosition
            if (position == 0) {
                requestDate.startTime = adapterView.getItemAtPosition(0).toString()
                holder.itemView.startTimeText.visibility = View.GONE
                return@onItemSelected
            }
            showSchedulingTimePicker(holder, requestDate)
        }
    }

    private fun showSchedulingTimePicker(holder: TimeOffDateHolder, requestDate: TimeOffRequestDate) = RadialTimePickerDialogFragment()
            .setStartTime(8, 0)
            .setForced24hFormat()
            .setThemeCustom(R.style.GreenBetterPickers)
            .setOnTimeSetListener { _, hourOfDay, minute ->
                requestDate.startTime = StringUtil.formattedTime(DateTime().withHourOfDay(hourOfDay).withMinuteOfHour(minute))
                holder.itemView.startTimeText.visibility = View.VISIBLE
                holder.itemView.startTimeText.text = requestDate.startTime
            }
            .setOnDismissListener { _ ->
                if (holder.itemView.startTimeText.visibility == View.GONE) {
                    holder.itemView.schedulingSpinner.setSelection(0, true)
                }
            }
            .show(fragmentManager, SCHEDULE_SPINNER_TIME_PICKER_TAG)

    override fun onGetSwipeReactionType(holder: TimeOffDateHolder, position: Int, x: Int, y: Int) = REACTION_CAN_SWIPE_LEFT or REACTION_CAN_SWIPE_RIGHT

    override fun onSwipeItemStarted(holder: TimeOffDateHolder, position: Int) = notifyDataSetChanged()

    override fun onSetSwipeBackground(holder: TimeOffDateHolder, position: Int, type: Int) = holder.itemView.setBackgroundResource(if (type == DRAWABLE_SWIPE_LEFT_BACKGROUND) R.drawable.bg_swipe_item_left else if (type == DRAWABLE_SWIPE_RIGHT_BACKGROUND) R.drawable.bg_swipe_item_right else R.drawable.bg_swipe_item_neutral)

    override fun onSwipeItem(holder: TimeOffDateHolder, position: Int, result: Int): SwipeResultAction? = if (result != RESULT_SWIPED_LEFT && result != RESULT_SWIPED_RIGHT) {
        null
    } else object : SwipeResultActionRemoveItem() {
        private var removedRequestDate: TimeOffRequestDate? = null

        override fun onPerformAction() {
            super.onPerformAction()
            removedRequestDate = requestDates.removeAt(position)
            notifyItemRemoved(position)
        }

        override fun onSlideAnimationEnd() {
            super.onSlideAnimationEnd()
            bus.post(OnTimeOffDateItemRemoved(position, removedRequestDate))
            tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Edit", "delete")
            tracker.trackFirebaseEvent(FirebaseEvents.EDIT_ITEM, ITEM_NAME, "delete")
            calculateRunningBalances()
        }
    }

    fun insertItem(position: Int, requestDate: TimeOffRequestDate) {
        requestDates.add(position, requestDate)
        notifyItemInserted(position)
        calculateRunningBalances()
    }

    private fun requestBalanceTypeThree() {
        if (!preferences.timeOffBalancesEnabled) {
            notifyDataSetChanged()
            return
        }
        Observable.from(this.requestDates)
                .map { it.effectiveDate }
                .distinct()
                .toList()
                .map { dates -> dates.toTypedArray() }
                .doOnNext { dates -> actionsCreator.getBalances(*dates) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted { notifyDataSetChanged() }
                .subscribe()
    }

    @Subscribe
    fun onEvent(event: OnDateBalancesServiceError) {
        runningBalances = LongSparseArray()
        notifyDataSetChanged()
    }

    @Subscribe
    fun onEvent(event: OnNewDateBalancesAvailable) {
        lastBalancesByBucket = event.balancesByBucket
        calculateRunningBalances()
    }

    private fun calculateRunningBalances() {
        runningBalances = LongSparseArray()
        if (!preferences.timeOffBalancesEnabled) {
            notifyDataSetChanged()
            return
        }
        val balancesByBucket = initializeTemporalBalancesByBucket()
        val takenSoFarByBucket = SparseArray<Double>()
        Observable.from(requestDates)
                .toSortedList { t1, t2 -> t1.compareTo(t2) }
                .concatMapIterable { it }
                .map { Pair(balancesMeta.getAccrualIdByPayTypeCode(it.payType), it) }
                .filter { it.first > -1 && balancesByBucket.get(it.first) != null }
                .doOnNext {
                    val hoursAmount = it.second.minutes.toDouble() / 60
                    if (!isBalanceTypeThreeUser) {
                        balancesByBucket.get(it.first)[BALANCE_TYPE_NOT_EQUAL_THREE_MAP_KEY] = (balancesByBucket.get(it.first)[BALANCE_TYPE_NOT_EQUAL_THREE_MAP_KEY] ?: hoursAmount) - hoursAmount
                        return@doOnNext
                    }
                    val takenSoFar = if (takenSoFarByBucket.get(it.first) == null) 0.0 else takenSoFarByBucket.get(it.first)
                    val remainingHours = (balancesByBucket.get(it.first)[it.second.effectiveDate] ?: (hoursAmount + takenSoFar)) - hoursAmount - takenSoFar
                    takenSoFarByBucket.put(it.first, takenSoFar + hoursAmount)
                    runningBalances.put(it.second.id, remainingHours)
                }
                .toList()
                .concatMapIterable { it }
                .doOnNext {
                    if (!isBalanceTypeThreeUser) {
                        runningBalances.put(it.second.id, balancesByBucket.get(it.first)[BALANCE_TYPE_NOT_EQUAL_THREE_MAP_KEY])
                    }
                }
                .doOnError {
                    Timber.e(it, it.message)
                    Crashlytics.logException(it)
                }
                .doOnCompleted { this.notifyDataSetChanged() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    private fun initializeTemporalBalancesByBucket(): SparseArray<MutableMap<String, Double>> {
        val balancesByBucket = SparseArray<MutableMap<String, Double>>()
        if (isBalanceTypeThreeUser && lastBalancesByBucket.size() > 0) {
            for (i in 0 until lastBalancesByBucket.size()) {
                val key = lastBalancesByBucket.keyAt(i)
                balancesByBucket.append(key, TreeMap(lastBalancesByBucket.get(key)))
            }
            return balancesByBucket
        }
        balances.let {
            for (balance in it.periodBalances) {
                balancesByBucket.put(balance.accrualId, TreeMap())
                balancesByBucket.get(balance.accrualId)[BALANCE_TYPE_NOT_EQUAL_THREE_MAP_KEY] = balance.dates[0].balance
            }
        }
        return balancesByBucket
    }

    @Throws(IllegalArgumentException::class)
    fun getRequestDates(): List<TimeOffRequestDate> {
        if (requestDates.isEmpty()) {
            throw IllegalArgumentException(context.getString(R.string.time_off_no_date_error_text))
        }
        for (requestDate in requestDates) {
            if (requestDate.minutes == 0) {
                throw IllegalArgumentException(context.getString(R.string.time_off_zero_hours_error_text))
            }
        }
        if (balancesMeta.allowNegativeBalance == 0) {
            for (i in 0 until runningBalances.size()) {
                runningBalances.get(runningBalances.keyAt(i))?.let {
                    if (it < 0) throw IllegalArgumentException(context.getString(R.string.time_off_negative_balance_error_text))
                }
            }
        }
        return requestDates
    }

    fun isDateListNotEmpty() = !requestDates.isEmpty()

    inner class TimeOffDateHolder(view: View) : AbstractSwipeableItemViewHolder(view) {

        override fun getSwipeableContainerView(): View? = itemView.dateContainer
    }
}
