package com.insperity.escmobile.net.gson.deserializer

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.insperity.escmobile.net.gson.ITAOrgLevelAttr
import com.insperity.escmobile.net.gson.ITAReferenceAttr
import com.insperity.escmobile.net.gson.PunchList
import java.lang.reflect.Type

class PunchListDeserializer : JsonDeserializer<PunchList.ReferenceData> {

    override fun deserialize(jsonElement: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PunchList.ReferenceData {
        val json = jsonElement.asJsonObject
        val optionListsJson = json.getAsJsonObject("optionLists") ?: json.getAsJsonObject("OptionLists")
        val optionLists = context.deserialize<PunchList.OptionLists>(optionListsJson, PunchList.OptionLists::class.java)
        val orgLevelLabelsJson: JsonArray? = json.getAsJsonArray("orgLevelLabels") ?: json.getAsJsonArray("OrgLevelLabels")
        val orgLevels = orgLevelLabelsJson?.let {
            optionListsJson.entrySet().filter { it.key.contains("rgLevelDepth") }.map { orgLevelEntry ->
                val orgLevelId = it.first { orgLevelEntry.key.split("rgLevelDepth")[1].toInt() == (it.asJsonObject.get("depth") ?: it.asJsonObject.get("Depth")).asInt }.asJsonObject
                val items = context.deserialize<List<List<ITAReferenceAttr>>>(orgLevelEntry.value.asJsonArray, TypeToken.getParameterized(ArrayList::class.java, object : TypeToken<List<ITAReferenceAttr>>() {}.type).type)
                return@map Pair(orgLevelEntry.key, ITAOrgLevelAttr((orgLevelId.get("depth") ?: orgLevelId.get("Depth")).asInt, (orgLevelId.get("name") ?: orgLevelId.get("Name")).asString, items))
            }.toMap()
        } ?: HashMap()
        return PunchList.ReferenceData(optionLists, orgLevels)
    }
}