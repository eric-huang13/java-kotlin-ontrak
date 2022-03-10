package com.insperity.escmobile.net.gson

data class DollarList(val content: Dollars, val referenceData: ReferenceData) {

    inner class Dollars(val dollar: List<Dollar>)

    class Dollar(val effectiveDate: ITAFieldAttr, val coverageStartDate: ITAFieldAttr, val coverageStopDate: ITAFieldAttr, val amount: ITAFieldAttr, val payType: ITAFieldAttr, val comment: ITAFieldAttr, val deviceNum: ITAFieldAttr, val sourceCode: ITAFieldAttr, val orgLevels: Map<String, ITAFieldAttr>,
                 val mileageId: ITAFieldAttr, val beginMiles: ITAFieldAttr?, val endMiles: ITAFieldAttr?, val totalMiles: ITAFieldAttr?, val ratePerMile: ITAFieldAttr?, val vehicleNumber: ITAFieldAttr?)

    class ReferenceData(val optionLists: OptionLists, val orgLevels: Map<String, ITAOrgLevelAttr>)

    inner class OptionLists(val deviceNum: List<List<ITAReferenceAttr>>, val sourceCode: List<List<ITAReferenceAttr>>, val payType: List<List<ITAReferenceAttr>>, val vehicleNumber: List<List<ITAReferenceAttr>>)
}
