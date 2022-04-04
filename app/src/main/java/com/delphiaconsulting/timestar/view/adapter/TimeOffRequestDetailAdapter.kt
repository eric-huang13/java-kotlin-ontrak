package com.delphiaconsulting.timestar.view.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.net.gson.TimeOffRequestDetails
import com.delphiaconsulting.timestar.util.StringUtil
import com.delphiaconsulting.timestar.util.TimeOffStatusUtil
import com.delphiaconsulting.timestar.util.TimeOffStatuses
import kotlinx.android.synthetic.main.time_off_date_header.view.*
import kotlinx.android.synthetic.main.time_off_request_detail_header_item.view.*
import kotlinx.android.synthetic.main.time_off_request_response_header_item.view.*
import kotlinx.android.synthetic.main.time_off_request_response_item.view.*

class TimeOffRequestDetailAdapter(context: Context, private val requestDetails: TimeOffRequestDetails)
    : TimeOffRequestDatesViewAdapter(context, requestDetails.content.request.requestDates, requestDetails.referenceData, requestDetails.content.request.requestDates[0].scheduling.security != 0) {

    companion object {
        private val HEADER_VIEW_TYPE = 1
        private val RESPONSES_SEPARATOR_VIEW_TYPE = 2
        private val RESPONSE_HEADER_VIEW_TYPE = 3
        private val RESPONSE_VIEW_TYPE = 4
    }

    private val responseItems = requestDetails.content.request.responseItems

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return HEADER_VIEW_TYPE
        }
        if (requestDates.size + 1 == position) {
            return RESPONSES_SEPARATOR_VIEW_TYPE
        }
        if (requestDates.size >= position) {
            return super.getItemViewType(position)
        }
        if (responseItems[position - itemCount + responseItems.size].isHeader()) {
            return RESPONSE_HEADER_VIEW_TYPE
        }
        return RESPONSE_VIEW_TYPE
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + responseItems.size + 2
    }

    override fun getRequestDateItem(position: Int): TimeOffRequestDetails.RequestDate {
        return super.getRequestDateItem(position - 1)
    }

    private fun getResponseItem(position: Int): TimeOffRequestDetails.ResponseItem {
        val responsePosition = position - itemCount + responseItems.size
        return responseItems[responsePosition]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == RESPONSES_SEPARATOR_VIEW_TYPE) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.time_off_request_responses_separator_item, parent, false)
            return object : RecyclerView.ViewHolder(v) {}
        }
        if (viewType == HEADER_VIEW_TYPE) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.time_off_request_detail_header_item, parent, false)
            return TimeOffRequestDetailHeaderHolder(v)
        }
        if (viewType == RESPONSE_HEADER_VIEW_TYPE) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.time_off_request_response_header_item, parent, false)
            return TimeOffRequestResponseHeaderHolder(v)
        }
        if (viewType == RESPONSE_VIEW_TYPE) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.time_off_request_response_item, parent, false)
            return TimeOffRequestResponseHolder(v)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == RESPONSES_SEPARATOR_VIEW_TYPE) {
            return
        }
        if (getItemViewType(position) == HEADER_VIEW_TYPE) {
            setupDetailHeader(viewHolder as TimeOffRequestDetailHeaderHolder)
            return
        }
        if (getItemViewType(position) == RESPONSE_HEADER_VIEW_TYPE) {
            setupDetailResponseHeader(viewHolder as TimeOffRequestResponseHeaderHolder, position)
            return
        }
        if (getItemViewType(position) == RESPONSE_VIEW_TYPE) {
            setupDetailResponse(viewHolder as TimeOffRequestResponseHolder, position)
            return
        }
        super.onBindViewHolder(viewHolder, position)
    }

    private fun setupDetailHeader(holder: TimeOffRequestDetailHeaderHolder) {
        holder.itemView.cancelledReferenceText.visibility = if (requestDetails.content.request.cancelledRequestId.value.toInt() > 0 ||
                requestDetails.content.request.computedRequestStatus == TimeOffStatuses.WITHDRAWN || requestDetails.content.request.computedRequestStatus == TimeOffStatuses.CANCELLED) View.VISIBLE else View.GONE
        holder.itemView.cancelledReferenceText.setText(when {
            requestDetails.content.request.computedRequestStatus == TimeOffStatuses.WITHDRAWN -> R.string.withdrawn_request_red_text
            requestDetails.content.request.computedRequestStatus == TimeOffStatuses.CANCELLED -> R.string.cancelled_request_red_text
            requestDetails.content.request.cancelledRequestId.value.toInt() > 0 -> R.string.withdrawn_request_to_approve_red_text
            else -> R.string.cancelled_request_red_text
        })
        holder.itemView.totalHoursText.text = StringUtil.hoursAndMinutes(requestDetails.content.request.totalRequestedMinutes)
        holder.itemView.statusText.text = requestDetails.content.request.requestStatus.value
        holder.itemView.statusImage.setImageResource(TimeOffStatusUtil.getStatusIconResId(TimeOffStatuses.from(requestDetails.content.request.requestStatus.value)))
        holder.itemView.submittedDateText.text = requestDetails.content.request.requestTimedate.value
        holder.itemView.commentText.text = if (requestDetails.content.request.comment.value.isEmpty()) context.getString(R.string.time_off_no_comment_text) else requestDetails.content.request.comment.value
        holder.itemView.schedulingText.visibility = if (canSchedule) View.VISIBLE else View.GONE
    }

    private fun setupDetailResponseHeader(holder: TimeOffRequestResponseHeaderHolder, position: Int) {
        val responseHeader = getResponseItem(position) as TimeOffRequestDetails.ResponseHeader
        holder.itemView.headerText.text = responseHeader.headerText
    }

    private fun setupDetailResponse(holder: TimeOffRequestResponseHolder, position: Int) {
        val requestResponse = getResponseItem(position) as TimeOffRequestDetails.RequestResponse
        holder.itemView.approverNameText.text = requestResponse.recipientName
        holder.itemView.responseStatusText.text = requestResponse.requestStatus
        holder.itemView.responseStatusImage.setImageResource(TimeOffStatusUtil.getStatusIconResId(requestResponse.computedRequestStatus))
        if (requestResponse.requestStatus == context.getString(R.string.time_off_unanswered)) {
            holder.itemView.responseDateContainer.visibility = View.GONE
            holder.itemView.commentContainer.visibility = View.GONE
            return
        }
        holder.itemView.dateText.text = requestResponse.responseTimedate
        holder.itemView.responseCommentText.text = if (requestResponse.comment.isEmpty()) context.getString(R.string.time_off_no_comment_text) else requestResponse.comment
        holder.itemView.responseDateContainer.visibility = View.VISIBLE
        holder.itemView.commentContainer.visibility = View.VISIBLE
    }

    inner class TimeOffRequestDetailHeaderHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class TimeOffRequestResponseHeaderHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class TimeOffRequestResponseHolder(view: View) : RecyclerView.ViewHolder(view)
}
