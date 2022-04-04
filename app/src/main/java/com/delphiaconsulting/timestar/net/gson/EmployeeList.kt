package com.delphiaconsulting.timestar.net.gson

class EmployeeList(val accessFlags: AccessFlags, val employeeList: List<Employee>, val payGroups: List<PayGroup>, val payPeriods: List<PayPeriod>) {

    inner class AccessFlags(val dollarsFlag: Int, val approvalAccessFlags: Int)

    inner class Employee(val employeeId: Int, val payGroupId: Int, val payPeriodId: Int, val employeeNumber: String, val firstName: String, val middleInitial: String, val lastName: String) {

        val name: String
            get() = "$lastName, $firstName $middleInitial".trim()
    }

    inner class PayGroup(val payGroupId: Int, val name: String, val payPeriodId: Int)

    inner class PayPeriod(val payPeriodId: Int, val name: String, val currentPeriod: PayPeriodDate)

    inner class PayPeriodDate(val idx: String, val startDate: String, val stopDate: String)
}
