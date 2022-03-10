package com.insperity.escmobile.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.insperity.escmobile.R
import com.insperity.escmobile.net.gson.TimeOffRequestDetails
import com.insperity.escmobile.util.StringUtil
import kotlinx.android.synthetic.main.time_off_view_date_item.view.*

@Suppress("LeakingThis")
open class TimeOffRequestDatesViewAdapter(protected val context: Context, protected val requestDates: List<TimeOffRequestDetails.RequestDate>, protected val referenceData: TimeOffRequestDetails.ReferenceData, protected val canSchedule: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return requestDates.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.time_off_view_date_item, parent, false)
        return TimeOffDateHolder(v)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val requestDate = getRequestDateItem(position)
        val holder = viewHolder as TimeOffDateHolder
        holder.itemView.dateText.text = requestDate.effectiveDate.value
        holder.itemView.hoursText.text = StringUtil.hoursAndMinutes(Integer.parseInt(requestDate.minutes.value))
        holder.itemView.payTypeText.text = referenceData.optionLists.getPayTypeLabelByCode(requestDate.payType.value, requestDate.payType.optionListIndex)
        holder.itemView.schedulingContainer.visibility = if (canSchedule) View.VISIBLE else View.GONE
        if (!canSchedule) return
        holder.itemView.schedulingText.text = referenceData.optionLists.getSchedulingLabelById(requestDate.scheduling.value, requestDate.scheduling.optionListIndex)
        holder.itemView.startTimeText.visibility = if (requestDate.scheduling.value == "0") View.GONE else View.VISIBLE
        holder.itemView.startTimeText.text = requestDate.startTime.value
    }

    protected open fun getRequestDateItem(position: Int): TimeOffRequestDetails.RequestDate {
        return requestDates[position]
    }

    inner class TimeOffDateHolder(view: View) : RecyclerView.ViewHolder(view)
}
