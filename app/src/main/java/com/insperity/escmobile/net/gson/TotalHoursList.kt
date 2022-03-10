package com.insperity.escmobile.net.gson

class TotalHoursList(val content: TotalHoursContent, val referenceData: TotalHoursReferenceData) {

    inner class TotalHoursContent(val employeeCalcTime: List<CalculatedTimeItem>)

    class CalculatedTimeItem(val effectiveDate: ITAFieldAttr, val payType: ITAFieldAttr, val shiftType: ITAFieldAttr, val lunchMinutes: ITAFieldAttr, val minutes: ITAFieldAttr, val punchId: ITAFieldAttr, val otherHoursId: ITAFieldAttr,
                             val startTimedate: ITAFieldAttr, val stopTimedate: ITAFieldAttr, val startType: ITAFieldAttr, val stopType: ITAFieldAttr, val sourceCode: ITAFieldAttr, val payRate: ITAFieldAttr) {

        var calcLunchMinutes: String? = null
        var calcMinutes: String? = null

        fun getLunchMinutes() = calcLunchMinutes?.toIntOrNull() ?: lunchMinutes.value.toIntOrNull() ?: 0

        fun getMinutes() = calcMinutes?.toIntOrNull() ?: minutes.value.toIntOrNull() ?: 0
    }

    inner class TotalHoursReferenceData(val approvalStatus: ApprovalStatusCode, val timeFormat: String, val access: AllowedAccess)

    inner class ApprovalStatusCode(val statusFlags: Int)

    inner class AllowedAccess(val hours: Boolean, val punches: Boolean, val dollars: Boolean)
}
