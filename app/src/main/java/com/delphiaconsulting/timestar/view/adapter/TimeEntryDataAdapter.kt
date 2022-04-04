package com.delphiaconsulting.timestar.view.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.event.OnTimeEntryItemClicked
import com.delphiaconsulting.timestar.view.common.TimeEntryColumnData
import com.delphiaconsulting.timestar.view.common.TimeEntryRowData
import kotlinx.android.synthetic.main.view_time_entry_lists_item.view.*
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by dsierra on 07/07/17.
 */
class TimeEntryDataAdapter(val context: Context, val bus: EventBus, private val supervisorAccessed: Boolean) : RecyclerView.Adapter<TimeEntryDataAdapter.TimeEntryDataHolder>() {

    private var properties: List<TimeEntryColumnData> = ArrayList()
    private var items: List<TimeEntryRowData> = ArrayList()

    fun setData(properties: List<TimeEntryColumnData>, items: List<TimeEntryRowData>) {
        this.properties = properties
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TimeEntryDataHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_time_entry_lists_item, parent, false))

    override fun onBindViewHolder(holder: TimeEntryDataHolder, position: Int) {
        val item = items[position]
        val columnViews = getColumnViews(holder)
        columnViews.forEach { it.visibility = View.GONE }
        for (i in 0 until item.data.size) {
            columnViews[i].text = item.data[i]
            columnViews[i].width = properties[i].finalWidth
            columnViews[i].visibility = View.VISIBLE
            columnViews[i].setTextColor(ContextCompat.getColor(context, if (item.highlighted) R.color.insperity_red else R.color.text_black))
            columnViews[i].gravity = properties[i].getGravity()
            columnViews[i].setPadding(0, 0, properties[i].getEndPadding(context), 0)
            if (i == 0 && item.detailItem != null && !item.highlighted) {
                columnViews[i].setTextColor(ContextCompat.getColor(context, R.color.text_link))
            }
        }
        item.detailItem?.let { detailItem -> holder.itemView.itemContainer.setOnClickListener { bus.postSticky(OnTimeEntryItemClicked(detailItem, supervisorAccessed)) } }
    }

    private fun getColumnViews(holder: TimeEntryDataHolder) = arrayOf(holder.itemView.column0, holder.itemView.column1, holder.itemView.column2, holder.itemView.column3, holder.itemView.column4,
            holder.itemView.column5, holder.itemView.column6, holder.itemView.column7, holder.itemView.column8, holder.itemView.column9)

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = 0

    inner class TimeEntryDataHolder(v: View) : RecyclerView.ViewHolder(v)
}