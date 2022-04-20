package com.delphiaconsulting.timestar.store.impl

import com.delphiaconsulting.timestar.action.Action
import com.delphiaconsulting.timestar.action.Actions
import com.delphiaconsulting.timestar.action.Keys
import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.event.OnLatestTimeOffRequestReceived
import com.delphiaconsulting.timestar.event.OnTimeOffApprovalPendingAmountReceived
import com.delphiaconsulting.timestar.event.OnTimeOffDataServiceError
import com.delphiaconsulting.timestar.net.gson.Response
import com.delphiaconsulting.timestar.net.gson.TimeOffApprovalRequests
import com.delphiaconsulting.timestar.net.gson.TimeOffRequestDetails
import com.delphiaconsulting.timestar.util.TimeOffStatuses
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

/**
 * Created by qktran on 1/23/17.
 */

@Singleton
class TimeOffStoreImpl(dispatcher: Dispatcher, bus: EventBus) : MainTimeOffStoreImpl(dispatcher, bus) {

    //@Suppress("UNCHECKED_CAST")
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
