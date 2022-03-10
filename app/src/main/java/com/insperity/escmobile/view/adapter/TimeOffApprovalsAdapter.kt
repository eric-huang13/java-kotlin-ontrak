package com.insperity.escmobile.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnTimeOffRequestClicked
import com.insperity.escmobile.net.gson.TimeOffApprovalRequests
import com.insperity.escmobile.util.FormatHelper
import com.insperity.escmobile.util.TimeOffStatusUtil
import kotlinx.android.synthetic.main.time_off_approval_list_item.view.*
import kotlinx.android.synthetic.main.time_off_request_list_empty_item.view.*
import org.greenrobot.eventbus.EventBus
import org.joda.time.format.DateTimeFormat
import java.util.*

/**
 * Created by dsierra on 07/07/17.
 */
class TimeOffApprovalsAdapter(private val context: Context, private val bus: EventBus) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private val TYPE_ITEM = 1
        private val TYPE_EMPTY = 2
    }

    var requests: List<TimeOffApprovalRequests.TimeOffApprovalRequest> = ArrayList()
        set(value) {
            field = value.sortedByDescending { it.requestId.value.toInt() }
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
            return TimeOffApprovalHolder(LayoutInflater.from(parent.context).inflate(R.layout.time_off_approval_list_item, parent, false))
        }
        return EmptyHolder(LayoutInflater.from(parent.context).inflate(R.layout.time_off_request_list_empty_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_EMPTY) {
            holder.itemView.messageText.setText(R.string.time_off_no_approval_requests)
            return
        }
        val request = getItem(position)
        holder.itemView.firstStatusImage.setImageResource(TimeOffStatusUtil.getStatusIconResId(request.computedRecipientStatus))
        holder.itemView.secondStatusImage.setImageResource(TimeOffStatusUtil.getStatusIconResId(request.computedRequestStatus))
        holder.itemView.secondStatusImage.visibility = if (request.computedRecipientStatus == request.computedRequestStatus) View.GONE else View.VISIBLE
        holder.itemView.nameText.text = request.employeeName.value
        val date = DateTimeFormat.forPattern("MM/dd/yyyy").parseDateTime(request.calculatedEffectiveDate.value).toDate()
        holder.itemView.dateText.text = FormatHelper.formattedDate(date)
        holder.itemView.commentText.text = if (request.comment.value.isNullOrEmpty()) context.getText(R.string.punch_no_comment_text) else request.comment.value
        holder.itemView.itemContainer.setOnClickListener { bus.post(OnTimeOffRequestClicked(request.requestId.value, holder.adapterPosition, request.computedRecipientStatus)) }
    }

    override fun getItemCount(): Int {
        if (requests.isEmpty()) {
            return 1
        }
        return requests.size
    }

    override fun getItemViewType(position: Int): Int {
        if (requests.isEmpty()) {
            return TYPE_EMPTY
        }
        return TYPE_ITEM
    }

    private fun getItem(position: Int): TimeOffApprovalRequests.TimeOffApprovalRequest {
        return requests[position]
    }

    private class EmptyHolder(v: View) : RecyclerView.ViewHolder(v)

    private class TimeOffApprovalHolder(v: View) : RecyclerView.ViewHolder(v)
}