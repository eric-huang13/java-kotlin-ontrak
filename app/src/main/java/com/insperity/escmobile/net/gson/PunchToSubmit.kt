package com.insperity.escmobile.net.gson

import org.joda.time.format.DateTimeFormat

class PunchToSubmit(val punchCategory: Long, val comment: String, @field:Transient val rawOrgLevels: List<OrgLevelSelection>, timestamp: Long, @field:Transient val id: Long = -1) {
    val datetime: String = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").print(timestamp)
    val orgLevels: List<OrgLevelSelection> = rawOrgLevels.filter { it.orgLevelId != 0L }

    val timestamp: Long
        get() = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").parseDateTime(datetime).millis

    class OrgLevelSelection(val depth: Long, val orgLevelId: Long)
}