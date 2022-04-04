package com.delphiaconsulting.timestar.view.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.delphiaconsulting.timestar.data.OrgItemEntity

class PunchOrgLevelAdapter(context: Context, val orgItems: List<OrgItemEntity>) : ArrayAdapter<OrgItemEntity>(context, android.R.layout.simple_spinner_item, orgItems) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView = super.getView(position, convertView, parent) as TextView
        textView.text = getItem(position)?.label
        return textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView = super.getDropDownView(position, convertView, parent) as TextView
        textView.visibility = if (position == count) View.GONE else View.VISIBLE
        textView.text = getItem(position)?.label
        return textView
    }

    override fun getCount(): Int {
        val count = super.getCount()
        return if (count > 1) count - 1 else count
    }
}
