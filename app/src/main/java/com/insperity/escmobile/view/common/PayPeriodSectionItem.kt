package com.insperity.escmobile.view.common

class PayPeriodSectionItem(override val sectionName: String?) : AdapterItem {

    override val isSection: Boolean
        get() = true

    override val id: Long
        get() = sectionName?.hashCode()?.toLong() ?: 0L
}