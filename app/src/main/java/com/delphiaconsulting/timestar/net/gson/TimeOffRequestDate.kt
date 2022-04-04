package com.delphiaconsulting.timestar.net.gson

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class TimeOffRequestDate(date: Date, var minutes: Int, var payType: String, var scheduling: Int, var startTime: String) : Comparable<TimeOffRequestDate> {

    @Transient val id = UUID.randomUUID().hashCode().toLong()

    var effectiveDate: String = DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime(date))
        private set

    fun setDate(date: Date) {
        this.effectiveDate = DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime(date))
    }

    val formattedDate: String?
        get() = DateTimeFormat.forPattern("MM/dd/yyyy").print(DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(effectiveDate))

    val dateTime: DateTime
        get() = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(effectiveDate)

    override fun compareTo(other: TimeOffRequestDate) = this.dateTime.compareTo(other.dateTime)
}