package com.insperity.escmobile.view.common

import com.insperity.escmobile.net.gson.PayPeriodList

class PayPeriodItem(val payPeriod: PayPeriodList.PayPeriod, val current: Boolean) : AdapterItem {

    override val sectionName: String?
        get() = null

    override val isSection: Boolean
        get() = false

    override val id: Long
        get() = payPeriod.hashCode().toLong()
}