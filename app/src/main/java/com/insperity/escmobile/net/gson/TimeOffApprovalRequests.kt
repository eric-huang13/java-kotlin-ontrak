package com.insperity.escmobile.net.gson

import com.insperity.escmobile.util.TimeOffStatusUtil
import com.insperity.escmobile.util.TimeOffStatuses

class TimeOffApprovalRequests(val content: ResponseContent) {

    inner class ResponseContent(val request: List<TimeOffApprovalRequest>)

    inner class TimeOffApprovalRequest(val requestId: RequestAttr, val calculatedEffectiveDate: RequestAttr, val comment: RequestAttr, val employeeName: RequestAttr, val employeeNumber: RequestAttr,
                                       val requestStatus: RequestAttr, val recipientStatus: RequestAttr, val requestItemSequence: RequestAttr, val recipientItemSequence: RequestAttr, val cancelledFlag: RequestAttr) {

        val computedRequestStatus: TimeOffStatuses
            get() = TimeOffStatusUtil.getRequestStatus(requestStatus.value, cancelledFlag.value.toInt())

        val computedRecipientStatus: TimeOffStatuses
            get() = TimeOffStatusUtil.getRecipientStatus(recipientStatus.value, computedRequestStatus, recipientItemSequence.value.toInt(), requestItemSequence.value.toInt())
    }

    inner class RequestAttr(val value: String, val security: Int) {
        override fun toString() = value
    }
}
