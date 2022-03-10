package com.insperity.escmobile.net.gson

class EmployeeBatch(val content: Content, val referenceData: ReferenceData) {

    inner class Content(val approvalStatus: List<ApprovalStatus>)

    inner class ApprovalStatus(val employeeId: Int, val data: EmployeeData?, val error: ErrorMessage?)

    inner class EmployeeData(val approvalStatus: ApprovalStatusCode, val minutes: Int, val dollars: String)

    inner class ApprovalStatusCode(val statusFlags: Int)

    inner class ReferenceData(val timeFormat: String)
}
