package com.delphiaconsulting.timestar.net.gson

import java.text.DecimalFormat

class TimeOffSummary(val content: SummaryContent) {

    inner class SummaryContent(val accrualBalance: List<AccrualBalance>)

    inner class AccrualBalance(val accrualBucket: ITAFieldAttr, val accrualBalance: ITAFieldAttr) {

        val formattedBalance: String
            get() = DecimalFormat("0.##").format(accrualBalance.value.toDouble())
    }
}
