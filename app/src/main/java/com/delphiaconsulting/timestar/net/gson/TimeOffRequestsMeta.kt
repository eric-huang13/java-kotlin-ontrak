package com.delphiaconsulting.timestar.net.gson

import org.joda.time.format.DateTimeFormat

/**
 * Represents metadata relevant to the creation of a time off request
 */
class TimeOffRequestsMeta(val content: ResponseContent, val referenceData: ReferenceData) {

    inner class ResponseContent(val request: RequestObject)

    inner class RequestObject(val recipientUsers: RecipientUser, val requestDates: List<RequestDate>)

    inner class RecipientUser(val security: Int, val rules: Rules, val optionListIndex: Int)

    inner class Rules(val min: Int, val max: Int)

    inner class RequestDate(val payType: ITAFieldAttr, val minutes: ITAFieldAttr, val scheduling: ITAFieldAttr, val effectiveDate: ITAFieldAttr, val startTime: ITAFieldAttr)

    inner class ReferenceData(val optionLists: OptionLists, val hoursIncrement: String, val startTime: String, val minDate: String, val defaultHours: String)

    inner class OptionLists(val payType: List<List<ITAReferenceAttr>>, val scheduling: List<List<ITAReferenceAttr>>, val recipientUsers: List<List<ITAReferenceAttr>>)

    val payTypesList: List<ITAReferenceAttr>
        get() = referenceData.optionLists.payType[content.request.requestDates[0].payType.optionListIndex ?: 0]

    val defaultPayTypeSelection: Int
        get() {
            val payTypes = payTypesList
            val index = payTypes.indexOf(payTypes.firstOrNull { it.value == content.request.requestDates[0].payType.value })
            return if (index != -1) index else 0
        }

    val schedulingList: List<ITAReferenceAttr>
        get() = referenceData.optionLists.scheduling[content.request.requestDates[0].scheduling.optionListIndex ?: 0]

    val submitToRecipients: List<ITAReferenceAttr>
        get() = referenceData.optionLists.recipientUsers[content.request.recipientUsers.optionListIndex]

    val minRecipients: Int
        get() = Math.min(content.request.recipientUsers.rules.min, referenceData.optionLists.recipientUsers[content.request.recipientUsers.optionListIndex].size)

    val maxRecipients: Int
        get() = content.request.recipientUsers.rules.max

    val defaultMinutesPayDay: Int
        get() {
            val defaultMinutes = content.request.requestDates[0].minutes.value.toInt()
            return if (incrementMinutes == 0 || defaultMinutes % incrementMinutes == 0) defaultMinutes else incrementMinutes
        }

    val canSchedule: Boolean
        get() = content.request.requestDates[0].scheduling.security != 0

    val incrementMinutes: Int
        get() = ((referenceData.hoursIncrement.toDoubleOrNull() ?: 1.0) * 60).toInt()

    val startTimeMinutes: Int
        get() {
            val datetime = DateTimeFormat.forPattern("h:mm a").parseDateTime(referenceData.startTime)
            return datetime.hourOfDay * 60 + datetime.minuteOfHour
        }
}