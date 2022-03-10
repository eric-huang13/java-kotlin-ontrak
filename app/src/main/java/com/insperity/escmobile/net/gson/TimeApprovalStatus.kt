package com.insperity.escmobile.net.gson

data class TimeApprovalStatus(val content: Content) {

    inner class Content(val results: List<ApprovalResult>)

    inner class ApprovalResult(val isSuccess: Boolean, val error: ErrorMessage?, val employeeId: Int)
}
