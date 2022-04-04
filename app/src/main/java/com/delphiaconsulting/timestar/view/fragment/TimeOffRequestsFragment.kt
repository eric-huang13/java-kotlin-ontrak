package com.delphiaconsulting.timestar.view.fragment

import android.os.Bundle
import android.view.*
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.action.creators.TimeOffActionsCreator
import com.delphiaconsulting.timestar.event.*
import com.delphiaconsulting.timestar.net.analytics.AnalyticsCategories
import com.delphiaconsulting.timestar.net.analytics.Tracker
import com.delphiaconsulting.timestar.store.TimeOffStore
import com.delphiaconsulting.timestar.view.activity.MainTimeOffSubmitActivity
import com.delphiaconsulting.timestar.view.activity.TimeOffRequestDetailsActivity
import com.delphiaconsulting.timestar.view.adapter.TimeOffRequestsAdapter
import com.delphiaconsulting.timestar.view.extension.snack
import kotlinx.android.synthetic.main.fragment_time_off_requests.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import javax.inject.Inject

class TimeOffRequestsFragment : BaseFragment() {

    companion object {
        val TAG: String = TimeOffRequestsFragment::class.java.simpleName
        const val TIME_OFF_REQUEST_ID_EXTRA = "TIME_OFF_REQUEST_ID_EXTRA"

        fun newInstance() = TimeOffRequestsFragment()
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var actionsCreator: TimeOffActionsCreator
    @Inject lateinit var store: TimeOffStore
    @Inject lateinit var tracker: Tracker

    private var adapter: TimeOffRequestsAdapter? = null
    private var requestEventId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_time_off_requests, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null && activity?.intent?.hasExtra(TIME_OFF_REQUEST_ID_EXTRA) == true) {
            requestEventId = activity?.intent?.getStringExtra(TIME_OFF_REQUEST_ID_EXTRA) ?: ""
        }
        component.inject(this)
        showProgressBar(true)
        actionsCreator.getTimeOffRequests()
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (adapter != null) {
            inflater.inflate(R.menu.menu_timeoff_add, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_add_request) {
            showProgressBar(true)
            actionsCreator.getRequestCreationData()
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe
    fun onEvent(event: OnTimeOffBalancesReceived) = context?.let {
        startActivity(MainTimeOffSubmitActivity.getCallingIntent(it))
        showProgressBar(false)
    }

    @Subscribe
    fun onEvent(event: OnTimeOffRequestsReceived) {
        context?.let {
            adapter = TimeOffRequestsAdapter(it, bus, event.timeOffRequests.content.request.sortedByDescending { it.requestId.value.toInt() })
            requestsContainer.visibility = View.VISIBLE
            requestList.adapter = adapter
            activity?.invalidateOptionsMenu()
        }
        if (requestEventId.isNotEmpty()) {
            actionsCreator.getTimeOffRequestDetails(requestEventId)
            return
        }
        showProgressBar(false)
    }

    @Subscribe
    fun onEvent(event: OnTimeOffRequestClicked) {
        showProgressBar(true)
        actionsCreator.getTimeOffRequestDetails(event.requestId)
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "Load Details", String.format(Locale.US, "Current -%d", event.position))
    }

    @Subscribe
    fun onEvent(event: OnTimeOffRequestDetailsReceived) {
        showProgressBar(false)
        requestEventId = ""
        context?.let { startActivity(TimeOffRequestDetailsActivity.getCallingIntent(it)) }
    }

    @Subscribe
    fun onEvent(event: OnTimeOffRequestDetailsError) {
        showProgressBar(false)
        requestEventId = ""
        requestsContainer.snack(event.message ?: getString(R.string.time_off_request_details_error_text)) {}
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeOffRequestSubmitted) {
        showProgressBar(true)
        actionsCreator.getTimeOffRequests()
        requestsContainer.snack(R.string.time_off_request_submitted_text) {}
        bus.removeStickyEvent(event)
    }
}
