package com.insperity.escmobile.view.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.insperity.escmobile.R
import com.insperity.escmobile.view.common.AdapterItem
import com.insperity.escmobile.view.common.TimeEntryItem
import com.insperity.escmobile.view.common.TimeEntrySeparator
import kotlinx.android.synthetic.main.view_time_entry_detail_item.view.*
import kotlinx.android.synthetic.main.view_time_entry_detail_section.view.*
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by dsierra on 07/07/17.
 */
class TimeEntryDetailDataAdapter(val context: Context, val bus: EventBus) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val SECTION_TYPE = 0
        const val SEPARATOR_TYPE = 1
        const val ITEM_TYPE = 2
    }

    private var items: List<AdapterItem> = ArrayList()

    fun setData(items: List<AdapterItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate: (Int) -> View = { LayoutInflater.from(context).inflate(it, parent, false) }
        return when (viewType) {
            SECTION_TYPE -> TimeEntrySectionHolder(inflate(R.layout.view_time_entry_detail_section))
            SEPARATOR_TYPE -> TimeEntrySeparatorHolder(inflate(R.layout.view_time_entry_detail_separator))
            else -> TimeEntryItemHolder(inflate(R.layout.view_time_entry_detail_item))
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (getItemViewType(position) == SEPARATOR_TYPE) return
        if (getItemViewType(position) == SECTION_TYPE) {
            viewHolder.itemView.sectionText.text = item.sectionName
            return
        }
        val holder = viewHolder as TimeEntryItemHolder
        if (item is TimeEntryItem) {
            holder.itemView.keyText.text = item.key
            holder.itemView.valueText.text = item.value
        }
        val lp = holder.itemView.layoutParams as RecyclerView.LayoutParams
        lp.setMargins(lp.leftMargin, if (position == 0) getDimension(R.dimen.full_margin) else 0, lp.rightMargin, if (position == (items.size - 1)) getDimension(R.dimen.full_margin) else 0)
        holder.itemView.layoutParams = lp
    }

    private fun getDimension(dimenRes: Int) = context.resources.getDimension(dimenRes).toInt()

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        if (items[position].isSection) {
            return SECTION_TYPE
        }
        if (items[position] is TimeEntrySeparator) {
            return SEPARATOR_TYPE
        }
        return ITEM_TYPE
    }

    inner class TimeEntrySectionHolder(v: View) : RecyclerView.ViewHolder(v)

    inner class TimeEntrySeparatorHolder(v: View) : RecyclerView.ViewHolder(v)

    inner class TimeEntryItemHolder(v: View) : RecyclerView.ViewHolder(v)
}