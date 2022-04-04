package com.delphiaconsulting.timestar.view.fragment


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.event.OnLatestTimeOffRequestReceived
import com.delphiaconsulting.timestar.event.OnTimeOffSummaryReceived
import com.delphiaconsulting.timestar.util.Preferences
import com.delphiaconsulting.timestar.util.StringUtil
import com.delphiaconsulting.timestar.util.TimeOffStatusUtil
import com.delphiaconsulting.timestar.view.activity.MainTimeOffRequestsActivity
import kotlinx.android.synthetic.main.fragment_time_off_request_widget.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class TimeOffRequestWidgetFragment : BaseFragment() {

    companion object {
        val TAG: String = TimeOffRequestWidgetFragment::class.java.simpleName

        fun newInstance() = TimeOffRequestWidgetFragment()
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var preferences: Preferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_time_off_request_widget, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
        if (preferences.timeOffRequestEnabled) {
            historyClickableContainer.setOnClickListener { activity?.let { startActivity(MainTimeOffRequestsActivity.getCallingIntent(it)) } }
        }
        //        TimeOffRequestDetails requestDetails = gson.fromJson("{\"content\": {\"request\": {\"requestId\": {\"value\": 127,\"security\": 1},\"requestType\": {\"value\": \"Time Off\",\"security\": 1},\"userId\": {\"value\": 19,\"security\": 1},\"requestWorkflowId\": {\"value\": 1,\"security\": 1},\"requestTimedate\": {\"value\": \"03/22/2017 11:15 AM\",\"security\": 1},\"requestStatus\": {\"value\": \"Unanswered\",\"security\": 1},\"cancelledFlag\": {\"value\": 0,\"security\": 1},\"cancelledRequestId\": {\"value\": 0,\"security\": 1},\"comment\": {\"value\": \"\",\"security\": 1},\"lastReconciledTimedate\": {\"value\": \"1900-01-01 00:00:00\",\"security\": 1},\"hiddenFlag\": {\"value\": 0,\"security\": 1},\"requestItemSequence\": {\"value\": 1,\"security\": 1},\"employeeName\": {\"value\": \"SHEEHAN, TERRI\",\"security\": 1},\"totalHoursRequested\": {\"value\": 24,\"security\": 0},\"responses\": [{\"comment\": \"\",\"recipientName\": \"DOSIER, TERESA\",\"requestItemSequence\": 1,\"requestStatus\": \"Unanswered\",\"responseTimedate\": \"\",\"userId\": 5}],\"requestDates\": [{\"effectiveDate\": {\"value\": \"03/22/2017\",\"security\": 1,\"rules\": {}},\"minutes\": {\"value\": 480,\"security\": 1},\"payType\": {\"value\": \"PTO\",\"security\": 1,\"optionListIndex\": 0},\"startTime\": {\"value\": \"\",\"security\": 1},\"scheduling\": {\"value\": 0,\"security\": 1,\"optionListIndex\": 0}},{\"effectiveDate\": {\"value\": \"03/23/2017\",\"security\": 1,\"rules\": {}},\"minutes\": {\"value\": 480,\"security\": 1},\"payType\": {\"value\": \"PTO\",\"security\": 1,\"optionListIndex\": 0},\"startTime\": {\"value\": \"11:15 AM\",\"security\": 1},\"scheduling\": {\"value\": 1,\"security\": 1,\"optionListIndex\": 0}},{\"effectiveDate\": {\"value\": \"03/24/2017\",\"security\": 1,\"rules\": {}},\"minutes\": {\"value\": 480,\"security\": 1},\"payType\": {\"value\": \"PTO\",\"security\": 1,\"optionListIndex\": 0},\"startTime\": {\"value\": \"11:15 AM\",\"security\": 1},\"scheduling\": {\"value\": 2,\"security\": 1,\"optionListIndex\": 0}}]}},\"referenceData\": {\"minDate\": \"02/04/2017\",\"hoursIncrement\": \"0.00\",\"defaultHours\": \"8.00\",\"startTime\": \"08:00 AM\",\"optionLists\": {\"payType\": [[{\"value\": \"OVT\",\"label\": \"Overtime\"},{\"value\": \"PTO\",\"label\": \"PTO\"},{\"value\": \"SIC\",\"label\": \"Sick\"},{\"value\": \"WKD\",\"label\": \"Weekend\"}]],\"scheduling\": [[{\"value\": 0,\"label\": \"All Day\"},{\"value\": 1,\"label\": \"Cancel Shift\"},{\"value\": 2,\"label\": \"Partial Day\"}]]}}}", TimeOffRequestDetails.class);
        //        actionsCreator.getLatestRequest(requestDetails);
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }

    @Subscribe
    fun onEvent(event: OnTimeOffSummaryReceived) {
        widgetContainer.visibility = View.VISIBLE
    }

    @Subscribe
    fun onEvent(event: OnLatestTimeOffRequestReceived) {
        requestContainer.visibility = View.VISIBLE
        noRequestText.visibility = View.GONE
        totalHoursText.text = StringUtil.decimalHours(event.requestDetails.content.request.totalRequestedMinutes.toDouble() / 60)
        statusImage.setImageResource(TimeOffStatusUtil.getStatusIconResId(event.requestDetails.content.request.computedRequestStatus))
        val requestDates = event.requestDetails.content.request.requestDates
        if (requestDates.isEmpty()) {
            return
        }
        datesText.text = if (requestDates.size == 1) requestDates[0].effectiveDate.value else String.format("%s - %s", requestDates[0].effectiveDate.value, requestDates[requestDates.size - 1].effectiveDate.value)
        requestClickableContainer.setOnClickListener {
            activity?.let { startActivity(MainTimeOffRequestsActivity.getCallingIntent(it).putExtra(TimeOffRequestsFragment.TIME_OFF_REQUEST_ID_EXTRA, event.requestDetails.content.request.requestId.value)) }
        }
    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        activity?.let {
            it.overridePendingTransition(0, 0)
            it.finish()
        }
    }
}
