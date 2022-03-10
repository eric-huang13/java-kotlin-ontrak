package com.insperity.escmobile.net.gson.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.insperity.escmobile.net.gson.DollarList
import com.insperity.escmobile.net.gson.ITAOrgLevelAttr
import com.insperity.escmobile.net.gson.ITAReferenceAttr
import java.lang.reflect.Type

class DollarListDeserializer : JsonDeserializer<DollarList.ReferenceData> {

    override fun deserialize(jsonElement: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DollarList.ReferenceData {
        val json = jsonElement.asJsonObject
        val optionListsJson = json.getAsJsonObject("optionLists") ?: json.getAsJsonObject("OptionLists")
        val optionLists = context.deserialize<DollarList.OptionLists>(optionListsJson, DollarList.OptionLists::class.java)
        val orgLevelLabelsJson = json.getAsJsonArray("orgLevelLabels") ?: json.getAsJsonArray("OrgLevelLabels")
        val orgLevels = optionListsJson.entrySet().filter { it.key.contains("rgLevelDepth") }.map { orgLevelEntry ->
            val orgLevelId = orgLevelLabelsJson.first { orgLevelEntry.key.split("rgLevelDepth")[1].toInt() == (it.asJsonObject.get("depth") ?: it.asJsonObject.get("Depth")).asInt }.asJsonObject
            val items = context.deserialize<List<List<ITAReferenceAttr>>>(orgLevelEntry.value.asJsonArray, TypeToken.getParameterized(ArrayList::class.java, object : TypeToken<List<ITAReferenceAttr>>() {}.type).type)
            return@map Pair(orgLevelEntry.key, ITAOrgLevelAttr((orgLevelId.get("depth") ?: orgLevelId.get("Depth")).asInt, (orgLevelId.get("name") ?: orgLevelId.get("Name")).asString, items))
        }.toMap()
        return DollarList.ReferenceData(optionLists, orgLevels)
    }
}