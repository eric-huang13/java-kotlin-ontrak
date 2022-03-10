package com.insperity.escmobile.net.gson

class PayPeriodList(val content: PayPeriods, val referenceData: ReferenceData) {

    inner class PayPeriods(val payPeriods: List<PayPeriod>, val currentPeriod: PayPeriodDate)

    inner class PayPeriod(val date: PayPeriodDate, val payGroupId: Int)

    inner class PayPeriodDate(val idx: String, val startDate: String, val stopDate: String) {

        val formattedPayPeriod: String
            get() = "$startDate - $stopDate"
    }

    inner class ReferenceData(val payGroups: List<PayGroup>, val employee: Employee)

    inner class PayGroup(val payGroupId: Int, val name: String)

    inner class Employee(val employeeNumber: String, val employeeId: Int, val firstName: String, val middleInitial: String, val lastName: String) {

        val fullName: String
            get() = "$firstName $middleInitial $lastName"
    }
}
