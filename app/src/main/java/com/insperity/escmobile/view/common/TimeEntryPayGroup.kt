package com.insperity.escmobile.view.common

import com.insperity.escmobile.net.gson.EmployeeList

class TimeEntryPayGroup(val id: Int, val name: String, val payPeriodIdx: String, val startDate: String, val stopDate: String, val employees: List<EmployeeList.Employee>) {

    val formattedPayPeriod: String
        get() = "$startDate - $stopDate"
}