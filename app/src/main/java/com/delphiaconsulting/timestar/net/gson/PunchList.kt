package com.delphiaconsulting.timestar.net.gson

import android.content.Context
import com.delphiaconsulting.timestar.R
import org.joda.time.format.DateTimeFormat
import java.util.*

class PunchList(val content: Punches, val referenceData: ReferenceData) {

    inner class Punches(val punch: List<Punch>)

    class Punch(val punchId: ITAFieldAttr, val actualTimedate: ITAFieldAttr, val comment: ITAFieldAttr, val punchCategory: ITAFieldAttr, val roundedTimedate: ITAFieldAttr, val roundSource: ITAFieldAttr, val payType: ITAFieldAttr, val shiftType: ITAFieldAttr, val deviceNum: ITAFieldAttr, val sourceCode: ITAFieldAttr, val employeeAttendanceRecords: List<EmployeeAttendanceRecord>?, val errorNumber: ITAFieldAttr?, val orgLevels: Map<String, ITAFieldAttr>) {

        val timestamp: Long
            get() = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm a").withLocale(Locale.ENGLISH).parseDateTime(actualTimedate.value).millis

        fun getInfoField(context: Context): String {
            if (employeeAttendanceRecords?.isNotEmpty() == true) {
                return employeeAttendanceRecords[0].category
            }
            if (errorNumber?.value == "M") {
                return context.getString(R.string.missing_punch_text)
            }
            if (comment.value.isNotEmpty()) {
                return context.getString(R.string.comment_text)
            }
            return context.getString(R.string.double_dash_text)
        }
    }

    inner class EmployeeAttendanceRecord(val category: String)

    class ReferenceData(val optionLists: OptionLists, val orgLevels: Map<String, ITAOrgLevelAttr>)

    inner class OptionLists(val payType: List<List<ITAReferenceAttr>>?, val shiftType: List<List<ITAReferenceAttr>>?, val roundSource: List<List<ITAReferenceAttr>>?, val deviceNum: List<List<ITAReferenceAttr>>?, val sourceCode: List<List<ITAReferenceAttr>>?, val punchCategory: List<List<ITAReferenceAttr>>)
}