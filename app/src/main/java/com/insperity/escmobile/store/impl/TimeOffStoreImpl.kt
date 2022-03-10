package com.insperity.escmobile.store.impl

import com.insperity.escmobile.action.Action
import com.insperity.escmobile.action.Actions
import com.insperity.escmobile.action.Keys
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.event.OnLatestTimeOffRequestReceived
import com.insperity.escmobile.event.OnTimeOffApprovalPendingAmountReceived
import com.insperity.escmobile.event.OnTimeOffDataServiceError
import com.insperity.escmobile.net.gson.Response
import com.insperity.escmobile.net.gson.TimeOffApprovalRequests
import com.insperity.escmobile.net.gson.TimeOffRequestDetails
import com.insperity.escmobile.util.TimeOffStatuses
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

/**
 * Created by qktran on 1/23/17.
 */

@Singleton
class TimeOffStoreImpl(dispatcher: Dispatcher, bus: EventBus) : MainTimeOffStoreImpl(dispatcher, bus) {

    @Suppress("UNCHECKED_CAST")
    override fun onActionReceived(action: Action) {
        when (action.type) {
            Actions.LATEST_TIME_OFF_REQUEST_RECEIVED -> {
                val requestDetails = action.getByKey(Keys.TIME_OFF_REQUEST_DETAILS) as Response<TimeOffRequestDetails>
                emitStickyChange(OnLatestTimeOffRequestReceived(requestDetails.data))
            }
            Actions.LATEST_TIME_OFF_REQUEST_ERROR -> handleCommonError(action)
            Actions.TIME_OFF_REQUESTS_FOR_PENDING_AMOUNT_RECEIVED -> {
                val pendingRequests = action.getByKey(Keys.TIME_OFF_REQUESTS) as Response<TimeOffApprovalRequests>
                processApprovalRequestsForPendingAmount(pendingRequests.data)
            }
            Actions.TIME_OFF_REQUESTS_FOR_PENDING_AMOUNT_ERROR -> handleCommonError(action)
            Actions.TIME_OFF_SUMMARY_ERROR -> {
                emitChange(OnTimeOffDataServiceError())
                super.onActionReceived(action)
            }
            else -> super.onActionReceived(action)
        }
    }

    private fun processApprovalRequestsForPendingAmount(requests: TimeOffApprovalRequests) {
        val unanswered = requests.content.request.filter {
            it.computedRequestStatus != TimeOffStatuses.CANCELLED && it.computedRequestStatus != TimeOffStatuses.WITHDRAWN &&
                    it.computedRecipientStatus != TimeOffStatuses.NO_ACTION && it.computedRecipientStatus != TimeOffStatuses.DECLINED && it.computedRecipientStatus != TimeOffStatuses.APPROVED
        }
        val uniqueRequestId = if (unanswered.size == 1) unanswered[0].requestId.value else null
        emitChange(OnTimeOffApprovalPendingAmountReceived(unanswered.size, uniqueRequestId))
    }
}
