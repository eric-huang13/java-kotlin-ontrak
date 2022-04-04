package com.delphiaconsulting.timestar.view.common

import com.delphiaconsulting.timestar.data.PunchEntity

/**
 * Created by dxsier on 1/11/17.
 */

class PunchItem(val punchEntity: PunchEntity) : AdapterItem {

    override val isSection: Boolean
        get() = false

    override val id: Long
        get() = "${punchEntity.datetime}${punchEntity.punchCategory.name}".hashCode().toLong()

    override val sectionName: String?
        get() = null
}
