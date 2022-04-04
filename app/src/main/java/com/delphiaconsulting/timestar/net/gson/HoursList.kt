package com.delphiaconsulting.timestar.net.gson

class HoursList(val content: HoursContent, val referenceData: ReferenceData) {

    inner class HoursContent(val hours: List<Hours>)

    class Hours(val comment: ITAFieldAttr, val deviceNum: ITAFieldAttr, val otherHoursId: ITAFieldAttr, val modifiedFlag: ITAFieldAttr, val actualDate: ITAFieldAttr, val payType: ITAFieldAttr, val minutes: ITAFieldAttr, val effectiveDate: ITAFieldAttr,
                val shiftType: ITAFieldAttr, val scheduleDeviationId: ITAFieldAttr, val allDayFlag: ITAFieldAttr, val startDate: ITAFieldAttr, val stopDate: ITAFieldAttr, val deviationCode: ITAFieldAttr, val orgLevels: Map<String, ITAFieldAttr>) {

        fun getMinutes() = minutes.value.toIntOrNull() ?: 0
    }

    class ReferenceData(val optionLists: OptionLists, val timeFormat: String, val orgLevels: Map<String, ITAOrgLevelAttr>)

    inner class OptionLists(val payType: List<List<ITAReferenceAttr>>, val deviceNum: List<List<ITAReferenceAttr>>, val allDayFlag: List<List<ITAReferenceAttr>>, val deviationCode: List<List<ITAReferenceAttr>>?, val sourceCode: List<List<ITAReferenceAttr>>,
                            val shiftType: List<List<ITAReferenceAttr>>)
}
