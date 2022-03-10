package com.insperity.escmobile.net.gson

import com.insperity.escmobile.util.TimeOffStatusUtil
import com.insperity.escmobile.util.TimeOffStatuses

class TimeOffRequests(val content: ResponseContent) {

    inner class ResponseContent(val request: List<TimeOffRequest>)

    inner class TimeOffRequest(val requestId: RequestAttr, val requestStatus: RequestAttr, val comment: RequestAttr, val requestTimedate: RequestAttr, val cancelledFlag: RequestAttr) {

        val computedRequestStatus: TimeOffStatuses
            get() = TimeOffStatusUtil.getRequestStatus(requestStatus.value, cancelledFlag.value.toInt())
    }

    inner class RequestAttr(val value: String, val security: Int) {

        override fun toString() = value
    }
}
