package com.delphiaconsulting.timestar.view.fragment

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.action.creators.TimeOffActionsCreator
import com.delphiaconsulting.timestar.event.*
import com.delphiaconsulting.timestar.net.analytics.AnalyticsCategories
import com.delphiaconsulting.timestar.net.analytics.Tracker
import com.delphiaconsulting.timestar.net.gson.TimeOffRequestDate
import com.delphiaconsulting.timestar.net.gson.TimeOffRequestsMeta
import com.delphiaconsulting.timestar.store.TimeOffStore
import com.delphiaconsulting.timestar.util.StringUtil
import com.delphiaconsulting.timestar.view.adapter.TimeOffSubmitToRowAdapter
import com.delphiaconsulting.timestar.view.extension.action
import com.delphiaconsulting.timestar.view.extension.snack
import kotlinx.android.synthetic.main.fragment_time_off_submit.*
import kotlinx.android.synthetic.main.time_off_date_header.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by qktran on 1/22/17.
 */
class TimeOffSubmitFragment : BaseFragment() {

    companion object {
        val TAG: String = TimeOffSubmitFragment::class.java.simpleName

        fun newInstance() = TimeOffSubmitFragment()
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var inputMethodManager: InputMethodManager
    @Inject lateinit var actionsCreator: TimeOffActionsCreator
    @Inject lateinit var store: TimeOffStore
    @Inject lateinit var tracker: Tracker

    private lateinit var requestMeta: TimeOffRequestsMeta
    private var adapter: TimeOffSubmitToRowAdapter? = null
    private var touchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var swipeManager: RecyclerViewSwipeManager? = null
    private var wrappedAdapter: RecyclerView.Adapter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_time_off_submit, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        inputMethodManager.hideSoftInputFromWindow(recyclerView.windowToken, 0)
        super.onStop()
    }

    override fun onDestroyView() {
        swipeManager?.let {
            it.release()
            swipeManager = null
        }
        touchActionGuardManager?.let {
            it.release()
            touchActionGuardManager = null
        }
        recyclerView.adapter = null
        wrappedAdapter?.let { WrapperAdapterUtils.releaseAll(it) }
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_timeoff_submit, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_submit_request) {
            submitRequest()
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeOffBalancesReceived) {
        requestMeta = event.timeOffRequestsMeta
        if (!requestMeta.canSchedule) {
            schedulingText.visibility = View.GONE
        }
        setupRecyclerView()
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Scheduling Allowed", requestMeta.canSchedule.toString())
    }

    private fun setupRecyclerView() {
        context?.applicationContext?.let {
            adapter = adapter ?: TimeOffSubmitToRowAdapter(it, childFragmentManager)
            adapter?.let {
                touchActionGuardManager = RecyclerViewTouchActionGuardManager()
                touchActionGuardManager?.setInterceptVerticalScrollingWhileAnimationRunning(true)
                touchActionGuardManager?.isEnabled = true
                swipeManager = RecyclerViewSwipeManager()
                wrappedAdapter = swipeManager?.createWrappedAdapter(it)
                recyclerView.layoutManager =
                    LinearLayoutManager(
                        context,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                recyclerView.adapter = wrappedAdapter
                val animator = SwipeDismissItemAnimator()
                animator.supportsChangeAnimations = false
                recyclerView.itemAnimator = animator
                touchActionGuardManager?.attachRecyclerView(recyclerView)
                swipeManager?.attachRecyclerView(recyclerView)
            }
        }
    }

    private fun submitRequest() {
        inputMethodManager.hideSoftInputFromWindow(recyclerView.windowToken, 0)
        try {
            adapter?.let {
                val comment = it.getComment()
                val recipients = it.getSelectedRecipients()
                val requestDates = it.getRequestDates()
                showProgressBar(true)
                actionsCreator.submitRequest(comment, recipients, requestDates)
                trackSubmissionEvents(comment, recipients, requestDates)
            }
        } catch (e: IllegalArgumentException) {
            Timber.e(e.message)
            showErrorSnackbar(e.message)
            tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Submit", String.format("Fail:%s", e.message))
        }
    }

    @Subscribe
    fun onEvent(event: OnTimeOffRequestSubmitted) {
        activity?.finish()
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Submit", "Success")
    }

    @Subscribe
    fun onEvent(event: OnTimeOffRequestSubmissionError) {
        showProgressBar(false)
        showErrorSnackbar(event.message ?: getString(R.string.time_off_request_submission_failed_error_text))
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Submit", "Fail")
    }

    fun pendingChanges() = adapter?.getComment()?.isNotEmpty() == true || adapter?.isDateListNotEmpty() == true

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeOffDatesSelected) {
        adapter?.setRequestDates(event.requestDates)
        bus.removeStickyEvent(event)
    }

    @Subscribe
    fun onEvent(event: OnTimeOffDateItemRemoved) = recyclerView.snack(R.string.date_removed_hint_text) {
        action(R.string.action_undo_text, ContextCompat.getColor(context, R.color.insperity_green_lightest)) {
            adapter?.insertItem(event.position, event.requestDate)
        }
    }

    private fun showErrorSnackbar(message: String?) = message?.let { recyclerView.snack(it) {} }

    private fun trackSubmissionEvents(comment: String, recipients: List<Int>, requestDates: List<TimeOffRequestDate>) {
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "New", "Complete")
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Comment", (!comment.isEmpty()).toString())
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Recipients", String.format(Locale.US, "%d:%d/%d", recipients.size, requestMeta.minRecipients, requestMeta.maxRecipients))
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Days", requestDates.size.toString())

        var minutes = 0
        val scheduling = ArrayList<Int>()
        var date: String
        val dates = ArrayList<String>()
        var payType: String
        val payTypes = ArrayList<String>()
        for (requestDate in requestDates) {
            minutes += requestDate.minutes
            if (!scheduling.contains(requestDate.scheduling)) {
                scheduling.add(requestDate.scheduling)
            }
            date = requestDate.effectiveDate
            if (!dates.contains(date)) {
                dates.add(date)
            }
            payType = requestDate.payType
            if (!payTypes.contains(payType)) {
                payTypes.add(payType)
            }
        }
        payTypes.sortBy { it }
        val payTypesStringBuilder = StringBuilder()
        for (p in payTypes) {
            if (payTypesStringBuilder.isNotEmpty()) {
                payTypesStringBuilder.append(", ")
            }
            payTypesStringBuilder.append(p)
        }

        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Hours", StringUtil.decimalHours((minutes.toFloat() / 60f).toDouble()))
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Hours Avg", StringUtil.decimalHours((minutes.toFloat() / 60f / dates.size.toFloat()).toDouble()))
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Calendar Days", dates.size.toString())
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Type", payTypesStringBuilder.toString())
        val schedulingStringBuilder = StringBuilder()
        for (schedule in requestMeta.schedulingList) {
            if (scheduling.contains(Integer.valueOf(schedule.value))) {
                if (schedulingStringBuilder.isNotEmpty()) {
                    schedulingStringBuilder.append(", ")
                }
                schedulingStringBuilder.append(schedule.label)
            }
        }
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Scheduling", schedulingStringBuilder.toString())
    }
}
