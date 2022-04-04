package com.delphiaconsulting.timestar.view.adapter

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_ANY
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.event.OnAddTimeOffDates
import com.delphiaconsulting.timestar.net.gson.ITAReferenceAttr
import com.delphiaconsulting.timestar.view.extension.addOnTextChanged
import com.delphiaconsulting.timestar.view.extension.onItemSelected
import com.delphiaconsulting.timestar.view.fragment.ListDialogFragment
import kotlinx.android.synthetic.main.time_off_submit_request_details_item.view.*


/**
 * Created by dxsier on 2/7/17.
 */

class TimeOffSubmitToRowAdapter(context: Context, fragmentManager: FragmentManager) : TimeOffRequestDatesEditAdapter(context, fragmentManager) {

    companion object {
        private const val SUBMIT_REQUEST_DETAILS_VIEW_TYPE = 1
        private const val LAST_ROW_ID = -1L
    }

    private var singleRecipientSelection = 0
    private var multipleRecipientsSelection = intArrayOf()
    private var recipientsText = ""
    private var comment = ""
    private var listDialogFragment: ListDialogFragment? = null

    override fun getItemViewType(position: Int) = if (requestDates.size != position) super.getItemViewType(position) else SUBMIT_REQUEST_DETAILS_VIEW_TYPE

    override fun getItemId(position: Int): Long = if (getItemViewType(position) != SUBMIT_REQUEST_DETAILS_VIEW_TYPE) super.getItemId(position) else LAST_ROW_ID

    override fun getItemCount() = super.getItemCount() + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = if (viewType != SUBMIT_REQUEST_DETAILS_VIEW_TYPE) super.onCreateViewHolder(parent, viewType) else
        TimeOffSubmitRequestDetailsHolder(LayoutInflater.from(parent.context).inflate(R.layout.time_off_submit_request_details_item, parent, false))

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) = if (getItemViewType(position) != SUBMIT_REQUEST_DETAILS_VIEW_TYPE) super.onBindViewHolder(viewHolder, position) else
        setupSubmitToViews(viewHolder as TimeOffSubmitRequestDetailsHolder)

    private fun setupSubmitToViews(holder: TimeOffSubmitRequestDetailsHolder) {
        holder.itemView.commentField.setText(comment)
        holder.itemView.commentField.setSelection(comment.length)
        holder.itemView.commentField.addOnTextChanged { comment = it }
        holder.itemView.addDateButton.setOnClickListener { bus.post(OnAddTimeOffDates()) }

        val recipients = requestMeta.submitToRecipients
        if (recipients.isEmpty()) return
        if (recipients.size == 1) {
            holder.itemView.submitToText.text = recipients[0].label
            holder.itemView.submitToText.visibility = View.VISIBLE
            return
        }
        recipients.let {
            if (singleSubmitRecipientSelection()) {
                setupSingleRecipientSelection(holder, it)
                return@let
            }
            setupMultipleRecipientSelection(holder, it)
        }
    }

    private fun singleSubmitRecipientSelection(): Boolean {
        val minRecipients = requestMeta.minRecipients
        return minRecipients == 1 && minRecipients == requestMeta.maxRecipients
    }

    private fun setupSingleRecipientSelection(holder: TimeOffSubmitRequestDetailsHolder, recipients: List<ITAReferenceAttr>) {
        holder.itemView.submitToSpinner.visibility = View.VISIBLE
        val adapter = ArrayAdapter(context, R.layout.spinner_textview, recipients)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.itemView.submitToSpinner.adapter = adapter
        holder.itemView.submitToSpinner.setSelection(singleRecipientSelection)
        holder.itemView.submitToSpinner.onItemSelected { _, position -> singleRecipientSelection = position }
    }

    private fun setupMultipleRecipientSelection(holder: TimeOffSubmitRequestDetailsHolder, recipients: List<ITAReferenceAttr>) {
        holder.itemView.submitToText.text = recipientsText
        holder.itemView.submitToText.visibility = View.VISIBLE
        holder.itemView.submitToText.setOnClickListener {
            listDialogFragment = ListDialogFragment.createBuilder(context, fragmentManager)
                    .setDismissible(false)
                    .setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE)
                    .setTitle(R.string.time_off_select_recipient_hint_text)
                    .setItems(recipients.map { it.label }.toTypedArray())
                    .setCheckedItems(multipleRecipientsSelection)
                    .setConfirmButtonText(R.string.ok_btn_text)
                    .setCancelButtonText(R.string.cancel_btn_text)
                    .setOnItemsSelectedListener { values, selectedPositions, _ ->
                        val minRecipientSelection = requestMeta.minRecipients
                        if (selectedPositions.size < minRecipientSelection) {
                            Toast.makeText(context, context.getString(R.string.min_recipient_selected_error, minRecipientSelection), Toast.LENGTH_LONG).show()
                            return@setOnItemsSelectedListener
                        }
                        val maxRecipientSelection = requestMeta.maxRecipients
                        if (selectedPositions.size > maxRecipientSelection) {
                            Toast.makeText(context, context.getString(R.string.max_recipient_selected_error, maxRecipientSelection), Toast.LENGTH_LONG).show()
                            return@setOnItemsSelectedListener
                        }
                        multipleRecipientsSelection = selectedPositions
                        recipientsText = String.format("%s%s", values[0], String.format(if (selectedPositions.size == 1) "" else " +%d", selectedPositions.size - 1))
                        holder.itemView.submitToText.text = recipientsText
                        listDialogFragment?.dismiss()
                    }
                    .show()
        }
    }

    override fun onGetSwipeReactionType(holder: TimeOffRequestDatesEditAdapter.TimeOffDateHolder, position: Int, x: Int, y: Int) = if (getItemViewType(position) == SUBMIT_REQUEST_DETAILS_VIEW_TYPE) REACTION_CAN_NOT_SWIPE_ANY else super.onGetSwipeReactionType(holder, position, x, y)

    fun getComment() = comment

    @Throws(IllegalArgumentException::class)
    fun getSelectedRecipients(): List<Int> {
        val selectedIds = ArrayList<Int>()
        val recipients = requestMeta.submitToRecipients
        if (recipients.size == 1 || singleSubmitRecipientSelection()) {
            selectedIds.add(recipients[singleRecipientSelection].value.toInt())
            return selectedIds
        }
        for (position in multipleRecipientsSelection) {
            selectedIds.add(recipients[position].value.toInt())
        }
        if (selectedIds.isEmpty()) throw IllegalArgumentException(context.getString(R.string.time_off_select_recipient_error_text))
        return selectedIds
    }

    inner class TimeOffSubmitRequestDetailsHolder(view: View) : RecyclerView.ViewHolder(view)
}
