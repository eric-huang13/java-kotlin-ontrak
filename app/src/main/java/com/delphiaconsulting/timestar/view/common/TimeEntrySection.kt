package com.delphiaconsulting.timestar.view.common

class TimeEntrySection(val name: String) : AdapterItem {
    override val isSection: Boolean
        get() = true
    override val id: Long
        get() = 0
    override val sectionName: String?
        get() = name
}