package com.insperity.escmobile.view.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnTimeOffRequestDetailsReceived
import com.insperity.escmobile.util.TimeOffStatuses
import com.insperity.escmobile.view.activity.MainTimeOffApprovalResolutionActivity
import kotlinx.android.synthetic.main.fragment_time_off_approval_details.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

/**
 * Created by dxsier on 07/12/17.
 */
class TimeOffApprovalDetailsFragment : BaseFragment() {

    companion object {
        private val ACTIVITY_REQUEST_CODE = 104
    }

    @Inject lateinit var bus: EventBus

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_time_off_approval_details, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            activity?.let {
                it.setResult(Activity.RESULT_OK)
                it.finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeOffRequestDetailsReceived) {
        nameText.text = event.requestDetails.content.request.employeeName.value
        if (event.requestDetails.content.request.computedRequestStatus == TimeOffStatuses.CANCELLED || event.requestDetails.content.request.computedRecipientStatus != TimeOffStatuses.UNANSWERED) {
            return
        }
        approveButton.visibility = View.VISIBLE
        declineButton.visibility = View.VISIBLE
        approveButton.setOnClickListener { onRequestResponded(true) }
        declineButton.setOnClickListener { onRequestResponded(false) }
    }

    private fun onRequestResponded(approved: Boolean) {
        activity?.let { startActivityForResult(MainTimeOffApprovalResolutionActivity.getCallingIntent(it, approved), ACTIVITY_REQUEST_CODE) }
    }
}
