package com.insperity.escmobile.util

import com.insperity.escmobile.R

object TimeOffStatusUtil {

    fun getStatusIconResId(status: TimeOffStatuses) = when (status) {
        TimeOffStatuses.UNANSWERED -> R.drawable.ic_time_off_unanswered
        TimeOffStatuses.APPROVED -> R.drawable.ic_time_off_approved
        TimeOffStatuses.DECLINED -> R.drawable.ic_time_off_declined
        TimeOffStatuses.CANCELLED -> R.drawable.ic_time_off_cancelled
        TimeOffStatuses.WITHDRAWN -> R.drawable.ic_time_off_cancelled
        TimeOffStatuses.ACKNOWLEDGED -> R.drawable.ic_time_off_acknowledged
        TimeOffStatuses.NO_ACTION -> R.drawable.ic_time_off_no_action
    }

    fun getRequestStatus(requestStatus: String, cancelledFlag: Int): TimeOffStatuses {
        val status = TimeOffStatuses.from(requestStatus)
        if (cancelledFlag == 1) {
            if (status == TimeOffStatuses.APPROVED) {
                return TimeOffStatuses.CANCELLED
            }
            if (status == TimeOffStatuses.UNANSWERED) {
                return TimeOffStatuses.WITHDRAWN
            }
        }
        return status
    }

    fun getRecipientStatus(recipientStatus: String, requestStatus: TimeOffStatuses, recipientItemSequence: Int, requestItemSequence: Int): TimeOffStatuses {
        if (TimeOffStatuses.from(recipientStatus) == TimeOffStatuses.UNANSWERED &&
                (requestStatus != TimeOffStatuses.UNANSWERED || recipientItemSequence < requestItemSequence)) {
            return TimeOffStatuses.NO_ACTION
        }
        return TimeOffStatuses.from(recipientStatus)
    }
}
