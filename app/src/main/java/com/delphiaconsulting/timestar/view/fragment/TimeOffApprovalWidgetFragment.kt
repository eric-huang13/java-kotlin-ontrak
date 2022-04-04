package com.delphiaconsulting.timestar.view.fragment

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.action.creators.TimeOffActionsCreator
import com.delphiaconsulting.timestar.event.OnTimeOffApprovalPendingAmountReceived
import com.delphiaconsulting.timestar.store.TimeOffStore
import com.delphiaconsulting.timestar.util.StringUtil
import com.delphiaconsulting.timestar.view.activity.MainTimeOffApprovalsActivity
import kotlinx.android.synthetic.main.fragment_time_off_approval_widget.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class TimeOffApprovalWidgetFragment : BaseFragment() {

    companion object {
        val TAG: String = TimeOffApprovalWidgetFragment::class.java.simpleName

        fun newInstance() = TimeOffApprovalWidgetFragment()
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var actionCreator: TimeOffActionsCreator
    @Inject lateinit var store: TimeOffStore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_time_off_approval_widget, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
        actionCreator.getPendingRequestAmount()
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
    fun onEvent(event: OnTimeOffApprovalPendingAmountReceived) {
        activity?.let {
            widgetContainer.visibility = View.VISIBLE
            val intent = MainTimeOffApprovalsActivity.getCallingIntent(it)
            var messageRes = R.string.time_off_approval_pending_requests_text
            if (event.pendingRequestsAmount == 1 && event.uniqueRequestId != null) {
                intent.putExtra(TimeOffApprovalsFragment.TIME_OFF_REQUEST_ID_EXTRA, event.uniqueRequestId)
                messageRes = R.string.time_off_approval_pending_request_text
            }
            clickableContainer.setOnClickListener { startActivity(intent) }

            if (event.pendingRequestsAmount == 0) return
            context?.let {
                hintText.text = StringUtil.setSpanBetweenTokens(getString(messageRes, event.pendingRequestsAmount), "##",
                        ForegroundColorSpan(ContextCompat.getColor(it, R.color.insperity_blue_deep)), StyleSpan(Typeface.BOLD))
            }
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