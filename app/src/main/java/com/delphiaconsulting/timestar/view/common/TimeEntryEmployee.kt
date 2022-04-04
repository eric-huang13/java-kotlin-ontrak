package com.delphiaconsulting.timestar.view.common

import com.delphiaconsulting.timestar.util.TimeEntryUtil.ENABLED_CHECKBOX_STATUS_ARRAY
import com.delphiaconsulting.timestar.view.extension.partOf

class TimeEntryEmployee(val employeeId: Int, val employeeName: String, val employeeNumber: String, val maskStatus: Int, val hours: String, val dollars: String, var error: String? = null, var selected: Boolean = false) {

    val canBeSelected: Boolean
        get() = maskStatus.partOf(ENABLED_CHECKBOX_STATUS_ARRAY) && error == null
}