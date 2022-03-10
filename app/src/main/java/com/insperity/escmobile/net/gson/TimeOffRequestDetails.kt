package com.insperity.escmobile.net.gson

import com.insperity.escmobile.util.TimeOffStatusUtil
import com.insperity.escmobile.util.TimeOffStatuses

class TimeOffRequestDetails(val content: ResponseContent, val referenceData: ReferenceData) {

    inner class ResponseContent(val request: RequestObject)

    inner class RequestObject(val lastReconciledTimedate: RequestAttr, val employeeName: RequestAttr, val hiddenFlag: RequestAttr, val requestType: RequestAttr, val totalHoursRequested: RequestAttr, val requestTimedate: RequestAttr, val userId: RequestAttr,
                              val requestId: RequestAttr, val requestWorkflowId: RequestAttr, val requestItemSequence: RequestAttr, val recipientItemSequence: RequestAttr, val cancelledRequestId: RequestAttr, val comment: RequestAttr, val requestStatus: RequestAttr,
                              val recipientStatus: RequestAttr, val cancelledFlag: RequestAttr, val responseCode: RequestAttr, val recipientUsers: RecipientUsers, val responses: List<RequestResponse>, val requestDates: List<RequestDate>) {
        val totalRequestedMinutes: Int
            get() = requestDates.sumBy { Integer.parseInt(it.minutes.value) }

        val computedRequestStatus: TimeOffStatuses
            get() = TimeOffStatusUtil.getRequestStatus(requestStatus.value, cancelledFlag.value.toInt())

        val computedRecipientStatus: TimeOffStatuses
            get() = TimeOffStatusUtil.getRecipientStatus(recipientStatus.value, computedRequestStatus, recipientItemSequence.value.toInt(), requestItemSequence.value.toInt())

        val responseItems: List<ResponseItem>
            get() {
                val responses = this.responses.sortedBy { it.requestItemSequence }
                var currentItemSequence = -1
                val responseItems = ArrayList<ResponseItem>()
                for (response in responses) {
                    if (response.requestItemSequence > requestItemSequence.value.toInt()) {
                        continue
                    }
                    if (response.requestItemSequence > currentItemSequence) {
                        responseItems.add(ResponseHeader(String.format("Sequence %d: %s", response.requestItemSequence,
                                if (computedRequestStatus != TimeOffStatuses.UNANSWERED || requestItemSequence.value.toInt() > response.requestItemSequence) "Complete" else "Active")))
                        currentItemSequence = response.requestItemSequence
                    }
                    responseItems.add(response)
                }
                return responseItems
            }
    }

    inner class RequestAttr(val value: String, val security: Int, val optionListIndex: Int)

    inner abstract class ResponseItem {
        abstract fun isHeader(): Boolean
    }

    inner class ResponseHeader(val headerText: String) : ResponseItem() {
        override fun isHeader() = true
    }

    inner class RequestResponse(val requestItemSequence: Int, val recipientName: String, val comment: String, val userId: Int, val requestStatus: String, val responseTimedate: String) : ResponseItem() {
        val computedRequestStatus: TimeOffStatuses
            get() = TimeOffStatuses.from(requestStatus)

        override fun isHeader() = false
    }

    inner class RequestDate(val payType: RequestAttr, val minutes: RequestAttr, val scheduling: RequestAttr, val startTime: RequestAttr, val effectiveDate: RequestAttr)

    inner class ReferenceData(val startTime: String, val minDate: String, val defaultHours: String, val hoursIncrement: String, val optionLists: OptionLists)

    inner class RecipientUsers(val security: Int, val optionListIndex: Int, val rules: Rules)

    inner class Rules(val min: Int, val max: Int)

    inner class OptionLists(val responseCode: List<List<LabelValueAttr>>, val generateDeviationFlag: List<List<LabelValueAttr>>, val autoHide: List<List<LabelValueAttr>>,
                            val scheduling: List<List<LabelValueAttr>>, val payType: List<List<LabelValueAttr>>, val recipientUsers: List<List<LabelValueAttr>>) {

        fun getPayTypeLabelByCode(code: String, index: Int) = payType[index].firstOrNull { it.value.contentEquals(code) }?.label

        fun getSchedulingLabelById(id: String, index: Int) = scheduling[index].firstOrNull { it.value.contentEquals(id) }?.label
    }

    inner class LabelValueAttr(val value: String, val label: String)
}
