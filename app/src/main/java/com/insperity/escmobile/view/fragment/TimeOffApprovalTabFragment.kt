package com.insperity.escmobile.view.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnTimeOffApprovalRequestsReceived
import com.insperity.escmobile.net.gson.TimeOffApprovalRequests
import com.insperity.escmobile.view.adapter.TimeOffApprovalsAdapter
import kotlinx.android.synthetic.main.fragment_time_off_approval_tab.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class TimeOffApprovalTabFragment : BaseFragment() {

    companion object {
        val TIME_OFF_APPROVAL_FRAGMENT_TYPE_EXTRA = "TIME_OFF_APPROVAL_FRAGMENT_TYPE_EXTRA"
        val UNANSWERED_TYPE = 0
        val APPROVED_TYPE = 1
        val DECLINED_TYPE = 2

        fun newInstance(fragmentType: Int): TimeOffApprovalTabFragment {
            val bundle = Bundle()
            bundle.putInt(TIME_OFF_APPROVAL_FRAGMENT_TYPE_EXTRA, fragmentType)
            val fragment = TimeOffApprovalTabFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject lateinit var bus: EventBus

    private var fragmentType: Int? = null
    private var requests: List<TimeOffApprovalRequests.TimeOffApprovalRequest>? = null
    private var adapter: TimeOffApprovalsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_time_off_approval_tab, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
        fragmentType = arguments?.getInt(TIME_OFF_APPROVAL_FRAGMENT_TYPE_EXTRA)
        if (savedInstanceState != null) {
            setupList()
        }
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }

    private fun setupList() {
        context?.let {
            if (adapter == null) {
                adapter = TimeOffApprovalsAdapter(it, bus)
            }
            adapter!!.requests = requests ?: ArrayList()
            if (requestList.adapter == null) {
                requestList.adapter = adapter
            }
        }
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeOffApprovalRequestsReceived) {
        when (fragmentType) {
            UNANSWERED_TYPE -> requests = event.unanswered
            APPROVED_TYPE -> requests = event.approved
            DECLINED_TYPE -> requests = event.declined
        }
        setupList()
    }
}