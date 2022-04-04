package com.delphiaconsulting.timestar.net.gson

class ITAFieldAttr(val value: String, val security: Int, val optionListIndex: Int? = null, val rules: RulesAttr? = null) {

    val sv: String
        get() = if (security != 0) value else ""

    override fun toString() = value

    inner class RulesAttr(val min: Int, val max: Int)
}
