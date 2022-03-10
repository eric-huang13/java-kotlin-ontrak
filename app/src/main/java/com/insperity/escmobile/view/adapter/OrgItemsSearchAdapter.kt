package com.insperity.escmobile.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.insperity.escmobile.R
import com.insperity.escmobile.data.OrgItemEntity
import com.insperity.escmobile.event.OnOrgItemClicked
import kotlinx.android.synthetic.main.org_item_row.view.*
import org.greenrobot.eventbus.EventBus

class OrgItemsSearchAdapter(private val bus: EventBus) : RecyclerView.Adapter<OrgItemsSearchAdapter.OrgItemHolder>(), Filterable {

    private var filteredOrgItems: MutableList<OrgItemEntity> = ArrayList()
    private var rawOrgItems: MutableList<OrgItemEntity> = ArrayList()

    fun setData(orgItems: List<OrgItemEntity>) {
        this.rawOrgItems = ArrayList(orgItems)
        setFilter("")
    }

    override fun getItemCount() = filteredOrgItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrgItemHolder = OrgItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.org_item_row, parent, false))

    override fun onBindViewHolder(holder: OrgItemHolder, position: Int) {
        val item = filteredOrgItems[position]
        holder.itemView.orgLevelText.text = item.label
        holder.itemView.orgLevelContainer.setOnClickListener {
            val index = rawOrgItems.indexOf(filteredOrgItems[holder.adapterPosition])
            bus.post(OnOrgItemClicked(index))
        }
    }

    fun setFilter(query: CharSequence) = filter.filter(query)

    override fun getFilter(): Filter = OrgItemsFilter()

    @Suppress("UNCHECKED_CAST")
    private inner class OrgItemsFilter : Filter() {

        override fun performFiltering(query: CharSequence): Filter.FilterResults? {
            var filteredItems = ArrayList(rawOrgItems).filter { it.id > 0L }
            if (!query.isEmpty()) {
                filteredItems = filteredItems.filter { it.label.toUpperCase().contains(query.toString().toUpperCase()) }
            }
            val results = Filter.FilterResults()
            results.count = filteredItems.size
            results.values = filteredItems
            return results
        }

        override fun publishResults(query: CharSequence, filterResults: Filter.FilterResults) {
            filteredOrgItems.clear()
            filteredOrgItems.addAll(filterResults.values as List<OrgItemEntity>)
            notifyDataSetChanged()
        }
    }

    inner class OrgItemHolder(view: View) : RecyclerView.ViewHolder(view)
}
