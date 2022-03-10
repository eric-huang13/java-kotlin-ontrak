package com.insperity.escmobile.view.adapter

import android.content.Context
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.insperity.escmobile.R
import com.insperity.escmobile.util.PunchStatus
import com.insperity.escmobile.view.common.AdapterItem
import com.insperity.escmobile.view.common.PunchItem
import kotlinx.android.synthetic.main.recent_punch_item.view.*
import kotlinx.android.synthetic.main.recent_punches_header_item.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class RecentPunchesAdapter(private val context: Context?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_HEADER = 2
    }

    private var items: List<AdapterItem> = ArrayList()
    private var expandedPosition = -1
    private var expandedId = -1L

    private lateinit var recyclerView: RecyclerView

    init {
        setHasStableIds(true)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun getItemViewType(position: Int): Int {
        if (items[position].isSection) return TYPE_HEADER
        return TYPE_ITEM
    }

    override fun getItemCount() = items.size

    private fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int): Long = getItem(position).id

    fun setAdapterItems(adapterItems: List<AdapterItem>) {
        items = adapterItems
        TransitionManager.beginDelayedTransition(recyclerView)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
            return PunchHolder(LayoutInflater.from(parent.context).inflate(R.layout.recent_punch_item, parent, false))
        }
        return HeaderHolder(LayoutInflater.from(parent.context).inflate(R.layout.recent_punches_header_item, parent, false))
    }

    @Suppress("SENSELESS_COMPARISON")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderHolder) {
            val item = getItem(position)
            holder.itemView.headerText.text = item.sectionName
            return
        }
        val item = getItem(position) as PunchItem
        val isMissingPunch = item.punchEntity.externalId != null && item.punchEntity.externalId < 0
        val hideUpperLine = position > 0 && position < items.size && getItem(position - 1).isSection
        holder.itemView.upperLine.visibility = if (hideUpperLine) View.GONE else View.VISIBLE
        val hideLowerLine = position == items.size - 1 || (position > 0 && getItem(position + 1).isSection)
        holder.itemView.lowerLine.visibility = if (hideLowerLine) View.GONE else View.VISIBLE
        holder.itemView.categoryCircleIndicator.setImageResource(getCategoryTypeCircleResource(item.punchEntity.punchCategoryId, isMissingPunch))

        holder.itemView.timeText.text = DateTime(item.punchEntity.datetime).toString(DateTimeFormat.forPattern("hh:mm aa"))
        val categoryDescription = item.punchEntity.punchCategory.description
        holder.itemView.titleText.text = if (categoryDescription.contains("Out ")) "Out" else categoryDescription
        context?.let {
            holder.itemView.timeText.setTextColor(ContextCompat.getColor(context, if (isMissingPunch) R.color.insperity_red else R.color.text_black))
            holder.itemView.titleText.setTextColor(ContextCompat.getColor(context, if (isMissingPunch) R.color.insperity_red else R.color.text_blue))
        }

        if (!isMissingPunch) {
            holder.itemView.punchContainer.setOnClickListener { handlePunchDetailsExpansion(holder.adapterPosition) }
        }
        holder.itemView.punchDetailsContainer.visibility = if (expandedId != item.id) View.GONE else View.VISIBLE
        holder.itemView.punchDetailsContainer.removeAllViews()
        val comment = item.punchEntity.comment
        if (expandedId == item.id) {
            expandedPosition = position
            context?.let {
                holder.itemView.punchDetailsContainer.addView(getDetailRow(it.getString(R.string.punch_comment_hint),
                        if (comment == null || comment.isEmpty()) it.getString(R.string.punch_no_comment_text) else Html.escapeHtml(comment).replace("&#10;", " "), it))
                val naString = it.getString(R.string.n_a_punch_text)
                for (orgLevelSelection in item.punchEntity.orgLevels) {
                    holder.itemView.punchDetailsContainer.addView(getDetailRow(orgLevelSelection.orgLevel.name, if (orgLevelSelection.orgItem == null) naString else orgLevelSelection.orgItem.label, it))
                }
            }
        }

        holder.itemView.statusImage.setImageResource(0)
        holder.itemView.statusColorContainer.setBackgroundResource(0)
        if (item.punchEntity.syncStatus != PunchStatus.ARCHIVED_STATUS) {
            holder.itemView.statusImage.setImageResource(if (item.punchEntity.syncStatus == PunchStatus.NOT_SYNCED_STATUS) R.drawable.ic_punch_not_synced else R.drawable.ic_punch_synced)
            holder.itemView.statusColorContainer.setBackgroundResource(if (item.punchEntity.syncStatus == PunchStatus.NOT_SYNCED_STATUS) R.drawable.punch_not_synced_container_bg else R.drawable.punch_synced_container_bg)
            return
        }
        if (!comment.isNullOrBlank()) {
            holder.itemView.statusImage.setImageResource(R.drawable.ic_punch_comment_blue)
        }
    }

    private fun getDetailRow(key: String, value: String, context: Context): View {
        val detailView = LayoutInflater.from(context).inflate(R.layout.punch_detail_item, null)
        detailView.findViewById<TextView>(R.id.keyText).text = key
        detailView.findViewById<TextView>(R.id.valueText).text = value
        return detailView
    }

    private fun handlePunchDetailsExpansion(position: Int) {
        if (expandedPosition == position) {
            expandedPosition = -1
            expandedId = -1
            recyclerView.itemAnimator = DefaultItemAnimator()
            notifyItemChanged(position)
            return
        }
        val previousExpandedPosition = expandedPosition
        expandedPosition = position
        expandedId = getItem(position).id
        recyclerView.itemAnimator = null
        TransitionManager.beginDelayedTransition(recyclerView)
        if (previousExpandedPosition > -1) {
            notifyItemChanged(previousExpandedPosition)
        }
        notifyItemChanged(expandedPosition)
    }

    private fun getCategoryTypeCircleResource(categoryId: Long, missingPunch: Boolean) = when (Math.abs(categoryId)) {
        0L, 1L, 2L, 3L, 4L, 5L, 6L, 17L, 19L, 21L, 31L, 34L, 35L -> if (missingPunch) R.drawable.ic_punch_green_stroke_circle else R.drawable.ic_punch_green_circle
        90L, 92L -> if (missingPunch) R.drawable.ic_punch_red_stroke_circle else R.drawable.ic_punch_red_circle
        else -> if (missingPunch) R.drawable.ic_punch_yellow_stroke_circle else R.drawable.ic_punch_yellow_circle
    }

    private class HeaderHolder(v: View) : RecyclerView.ViewHolder(v)

    private class PunchHolder(v: View) : RecyclerView.ViewHolder(v)
}
