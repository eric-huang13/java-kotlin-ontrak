package com.delphiaconsulting.timestar.util

/**
 * Created by dxsier on 8/14/17.
 */
enum class TimeOffStatuses {
    UNANSWERED,
    APPROVED,
    DECLINED,
    CANCELLED,
    WITHDRAWN,
    ACKNOWLEDGED,
    NO_ACTION;

    companion object {

        fun from(value: String) = when (value) {
            "UNANS" -> UNANSWERED
            "Unanswered" -> UNANSWERED
            "APPR" -> APPROVED
            "Approved" -> APPROVED
            "DECL" -> DECLINED
            "Declined" -> DECLINED
            "ACK" -> ACKNOWLEDGED
            "Acknowledged" -> ACKNOWLEDGED
            "Cancelled" -> CANCELLED
            "NoAction" -> NO_ACTION
            "Recommend Denial" -> DECLINED
            "Recommend Approval" -> APPROVED
            else -> NO_ACTION
        }
    }
}