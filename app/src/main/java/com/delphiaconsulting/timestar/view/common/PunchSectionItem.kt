package com.delphiaconsulting.timestar.view.common

/**
 * Created by dxsier on 1/11/17.
 */

class PunchSectionItem(override val sectionName: String) : AdapterItem {

    override val isSection: Boolean
        get() = true

    override val id: Long
        get() = sectionName.hashCode().toLong()
}
