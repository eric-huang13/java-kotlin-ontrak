package com.delphiaconsulting.timestar.view.common

import com.delphiaconsulting.timestar.net.gson.EmployeeList

class TimeEntryPayGroup(val id: Int, val name: String, val payPeriodIdx: String, val startDate: String, val stopDate: String, val employees: List<EmployeeList.Employee>) {

    val formattedPayPeriod: String
        get() = "$startDate - $stopDate"
}