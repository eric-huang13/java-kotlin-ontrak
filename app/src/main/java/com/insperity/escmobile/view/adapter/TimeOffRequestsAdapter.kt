package com.insperity.escmobile.view.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnTimeOffRequestClicked
import com.insperity.escmobile.net.gson.TimeOffRequests
import com.insperity.escmobile.util.FormatHelper
import com.insperity.escmobile.util.TimeOffStatusUtil
import kotlinx.android.synthetic.main.time_off_request_list_item.view.*
import org.greenrobot.eventbus.EventBus
import org.joda.time.format.DateTimeFormat
import java.util.*

/**
 * Created by qktran on 1/22/17.
 */
class TimeOffRequestsAdapter(private val context: Context, private val bus: EventBus, private val items: List<TimeOffRequests.TimeOffRequest>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private val TYPE_ITEM = 1
        private val TYPE_EMPTY = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
            return TimeOffRequestHolder(LayoutInflater.from(parent.context).inflate(R.layout.time_off_request_list_item, parent, false))
        }
        return EmptyHolder(LayoutInflater.from(parent.context).inflate(R.layout.time_off_request_list_empty_item, parent, false))
    }

    @Suppress("SENSELESS_COMPARISON")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder !is TimeOffRequestHolder) {
            return
        }
        val item = getItem(position)
        if (item.requestTimedate != null) {
            val date = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm a").withLocale(Locale.ENGLISH).parseDateTime(item.requestTimedate.value).toDate()
            holder.itemView.dateText.text = FormatHelper.formattedDate(date)
        }
        holder.itemView.commentText.text = if (item.comment.value.isNullOrEmpty()) context.getText(R.string.punch_no_comment_text) else item.comment.value
        holder.itemView.statusImage.setImageResource(TimeOffStatusUtil.getStatusIconResId(item.computedRequestStatus))
        holder.itemView.itemContainer.setOnClickListener { bus.post(OnTimeOffRequestClicked(item.requestId.value, holder.getAdapterPosition(), null)) }
    }

    override fun getItemCount(): Int {
        if (items.isEmpty()) {
            return 1
        }
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        if (items.isEmpty()) {
            return TYPE_EMPTY
        }
        return TYPE_ITEM
    }

    private fun getItem(position: Int) = items[position]

    private class EmptyHolder(v: View) : RecyclerView.ViewHolder(v)

    private class TimeOffRequestHolder(v: View) : RecyclerView.ViewHolder(v)
}
