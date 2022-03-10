package com.insperity.escmobile.view.fragment

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.avast.android.dialogs.fragment.SimpleDialogFragment
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnTimeOffBalancesReceived
import com.insperity.escmobile.event.OnTimeOffDatesSelected
import com.insperity.escmobile.net.gson.ITAReferenceAttr
import com.insperity.escmobile.net.gson.TimeOffRequestDate
import com.insperity.escmobile.net.gson.TimeOffRequestsMeta
import com.insperity.escmobile.store.TimeOffStore
import com.insperity.escmobile.util.AppUtil
import com.insperity.escmobile.util.StringUtil
import com.insperity.escmobile.view.common.MonthDecorator
import com.squareup.timessquare.CalendarCellDecorator
import com.squareup.timessquare.CalendarPickerView
import kotlinx.android.synthetic.main.fragment_time_off_add_date.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject

class TimeOffAddDateFragment : BaseFragment() {

    companion object {
        val TAG: String = TimeOffAddDateFragment::class.java.simpleName
        private const val START_TIME_PICKER = "START_TIME_PICKER"

        fun newInstance() = TimeOffAddDateFragment()
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var store: TimeOffStore
    @Inject lateinit var appUtil: AppUtil

    private lateinit var requestMeta: TimeOffRequestsMeta
    private var minutesPerDay: Int = 0
    private var startTimeMinutes: Int = 0
    private var selectedDates: List<Date>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        selectedDates = ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_time_off_add_date, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        selectedDates = calendarPicker.selectedDates
        bus.unregister(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_timeoff_add_date, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_add_dates) {
            onDatesSelected()
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeOffBalancesReceived) {
        requestMeta = event.timeOffRequestsMeta
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        calendarPicker.dividerHeight = appUtil.dpToPx(25f)
        calendarPicker.init(DateTime.now().dayOfYear().withMinimumValue().toDate(), DateTime.now().plusYears(1).dayOfYear().withMaximumValue().toDate())
                .inMode(CalendarPickerView.SelectionMode.MULTIPLE).withSelectedDates(selectedDates)
        val decorators = ArrayList<CalendarCellDecorator>()
        decorators.add(MonthDecorator(context))
        calendarPicker.decorators = decorators

        setHoursText(requestMeta.defaultMinutesPayDay)
        setStartTimeText(requestMeta.startTimeMinutes)

        val payTypeAdapter = ArrayAdapter<ITAReferenceAttr>(context, R.layout.spinner_textview, requestMeta.payTypesList)
        payTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        payTypeSpinner.adapter = payTypeAdapter
        payTypeSpinner.setSelection(requestMeta.defaultPayTypeSelection, true)

        val scheduleAdapter = ArrayAdapter<ITAReferenceAttr>(context, R.layout.spinner_textview, requestMeta.schedulingList)
        scheduleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        scheduleSpinner.adapter = scheduleAdapter
        if (!requestMeta.canSchedule) return
        schedulingContainer.visibility = View.VISIBLE
        scheduleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                startTimeContainer.visibility = if (position == 0) View.GONE else View.VISIBLE
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }

    private fun setupListeners() {
        hoursText.setOnClickListener { _ ->
            TimePickerDialogFragment.newInstance(incrementMinutes = requestMeta.incrementMinutes, preselectedMinutes = minutesPerDay)
                    .setOnTimeValueSetListener { setHoursText(it) }
                    .show(fragmentManager, TimePickerDialogFragment.TAG)
        }
        startTimeText.setOnClickListener {
            RadialTimePickerDialogFragment().setOnTimeSetListener { _, hourOfDay, minute -> setStartTimeText(hourOfDay * 60 + minute) }
                    .setStartTime(startTimeMinutes / 60, startTimeMinutes % 60)
                    .setForced24hFormat()
                    .setThemeCustom(R.style.GreenBetterPickers)
                    .show(childFragmentManager, START_TIME_PICKER)
        }
        previousMonthButton.setOnClickListener { calendarPicker.setSelection(calendarPicker.firstVisiblePosition - 1) }
        nextMonthButton.setOnClickListener { calendarPicker.setSelection(calendarPicker.firstVisiblePosition + 1) }
    }

    private fun onDatesSelected() {
        val dates = calendarPicker.selectedDates
        if (dates.isEmpty()) {
            SimpleDialogFragment.createBuilder(context, childFragmentManager)
                    .setMessage(getString(R.string.time_off_empty_dates_error_text))
                    .setPositiveButtonText(R.string.ok_btn_text)
                    .show()
            return
        }
        val requestDates = ArrayList<TimeOffRequestDate>()
        var formattedStartTime = ""
        dates.sortBy { DateTime(it) }
        for (date in dates) {
            if (scheduleSpinner.selectedItemPosition > 0) {
                formattedStartTime = StringUtil.formattedTime(DateTime().withHourOfDay(startTimeMinutes / 60).withMinuteOfHour(startTimeMinutes % 60))
            }
            val timeOffRequestDate = TimeOffRequestDate(date, minutesPerDay, (payTypeSpinner.selectedItem as ITAReferenceAttr).value, Integer.valueOf((scheduleSpinner.selectedItem as ITAReferenceAttr).value), formattedStartTime)
            requestDates.add(timeOffRequestDate)
        }
        bus.postSticky(OnTimeOffDatesSelected(requestDates))
    }

    private fun setHoursText(minutes: Int) {
        minutesPerDay = minutes
        hoursText.text = StringUtil.hoursAndMinutes(minutesPerDay)
    }

    private fun setStartTimeText(minutes: Int) {
        startTimeMinutes = minutes
        val hour = startTimeMinutes / 60
        val minute = startTimeMinutes % 60
        startTimeText.text = StringUtil.formattedTime(DateTime().withHourOfDay(hour).withMinuteOfHour(minute))
    }
}
