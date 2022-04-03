package com.insperity.escmobile.view.adapter

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.avast.android.dialogs.fragment.SimpleDialogFragment
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnApproveCheckboxCheckedChanged
import com.insperity.escmobile.event.OnTimeEntryEmployeeClicked
import com.insperity.escmobile.util.TimeEntryUtil.DISABLED_CHECKBOX_STATUS_ARRAY
import com.insperity.escmobile.util.TimeEntryUtil.DONE_CHECK_MARK_STATUS_ARRAY
import com.insperity.escmobile.util.TimeEntryUtil.EMPLOYEE_APPROVED_STATUS_ARRAY
import com.insperity.escmobile.util.TimeEntryUtil.ENABLED_CHECKBOX_STATUS_ARRAY
import com.insperity.escmobile.view.common.TimeEntryEmployee
import com.insperity.escmobile.view.extension.partOf
import kotlinx.android.synthetic.main.view_time_entry_employee_item.view.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by dsierra on 07/07/17.
 */
class TimeEntryEmployeeAdapter(val bus: EventBus, val fragmentType: Int?, val context: Context, val fragmentManager: FragmentManager, empAppDisabled: Boolean, dollarsDisabled: Boolean) : RecyclerView.Adapter<TimeEntryEmployeeAdapter.TimeEntryEmployeeHolder>() {

    private var widthArray = getItemMeasures(empAppDisabled, dollarsDisabled, context)
    private var items: MutableList<TimeEntryEmployee> = ArrayList()

    private fun getItemMeasures(empAppDisabled: Boolean, dollarsDisabled: Boolean, context: Context): List<Int> {
        val screenWidth = context.resources.displayMetrics.widthPixels - context.resources.getDimension(R.dimen.full_and_half_margin)
        var widthArray: MutableList<Int> = ArrayList()
        widthArray.addAll(context.resources.let {
            arrayOf(it.getDimension(R.dimen.sup_list_checkbox_width), it.getDimension(R.dimen.sup_list_name_item_width), it.getDimension(if (empAppDisabled) R.dimen.sup_list_zero_width else R.dimen.sup_list_emp_app_item_width),
                    it.getDimension(R.dimen.sup_list_total_item_width), it.getDimension(if (dollarsDisabled) R.dimen.sup_list_zero_width else R.dimen.sup_list_dollars_width))
        }.map { it.toInt() })
        val headerWidth = widthArray.reduce { accumulator, width -> accumulator + width }.toInt()
        if (screenWidth > headerWidth) {
            widthArray = ArrayList(widthArray.map { Math.round(it * screenWidth / headerWidth) })
        }
        return widthArray
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TimeEntryEmployeeHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_time_entry_employee_item, parent, false))

    override fun onBindViewHolder(holder: TimeEntryEmployeeHolder, position: Int) {
        val columnViews = getColumnViews(holder)
        val item = items[position]
        for (i in 0 until columnViews.size) {
            columnViews[i].layoutParams.width = widthArray[i]
        }
        holder.itemView.nameText.width = widthArray[0]
        holder.itemView.nameText.text = item.employeeName
        holder.itemView.numberText.text = item.employeeNumber
        holder.itemView.hoursText.text = item.hours
        holder.itemView.empAppText.setText(if (item.maskStatus.partOf(EMPLOYEE_APPROVED_STATUS_ARRAY)) R.string.yes_btn_text else R.string.no_btn_text)
        holder.itemView.empAppText.setTextColor(ContextCompat.getColor(context, if (item.maskStatus.partOf(DISABLED_CHECKBOX_STATUS_ARRAY)) R.color.text_grey_light else if (item.maskStatus.partOf(EMPLOYEE_APPROVED_STATUS_ARRAY)) R.color.insperity_green else R.color.insperity_red))
        holder.itemView.dollarsText.text = item.dollars
        holder.itemView.itemContainer.setBackgroundResource(if (item.maskStatus.partOf(DISABLED_CHECKBOX_STATUS_ARRAY)) R.color.button_grey_lightest else R.color.white)
        holder.itemView.itemContainer.setOnClickListener { bus.post(OnTimeEntryEmployeeClicked(item)) }
        holder.itemView.approveCheckbox.setOnCheckedChangeListener(null)
        holder.itemView.approveCheckbox.isChecked = item.selected
        holder.itemView.approveCheckbox.setOnCheckedChangeListener { _, checked ->
            items[holder.adapterPosition].selected = checked
            checkAllSelected()
        }
        when {
            item.error != null -> {
                showCheckbox(holder, false)
                holder.itemView.statusImage.setImageResource(R.drawable.ic_warning)
                holder.itemView.statusImage.setOnClickListener { SimpleDialogFragment.createBuilder(context, fragmentManager).setMessage(item.error).setPositiveButtonText(R.string.ok_btn_text).show() }
            }
            item.maskStatus.partOf(DONE_CHECK_MARK_STATUS_ARRAY) -> {
                showCheckbox(holder, false)
                holder.itemView.statusImage.setImageResource(R.drawable.ic_check)
                holder.itemView.statusImage.setOnClickListener(null)
            }
            item.maskStatus.partOf(ENABLED_CHECKBOX_STATUS_ARRAY) -> showCheckbox(holder, true)
            item.maskStatus.partOf(DISABLED_CHECKBOX_STATUS_ARRAY) -> showCheckbox(holder, true, false)
        }
    }

    private fun getColumnViews(holder: TimeEntryEmployeeHolder) = arrayOf(holder.itemView.checkboxContainer, holder.itemView.nameContainer, holder.itemView.empAppText, holder.itemView.hoursText, holder.itemView.dollarsText)

    private fun showCheckbox(holder: TimeEntryEmployeeHolder, show: Boolean, enable: Boolean = true) {
        holder.itemView.approveCheckbox.visibility = if (show) View.VISIBLE else View.INVISIBLE
        holder.itemView.statusImage.visibility = if (show) View.GONE else View.VISIBLE
        holder.itemView.approveCheckbox.isEnabled = enable
    }

    override fun getItemCount() = items.size

    fun insertItems(items: List<TimeEntryEmployee>) {
        removeItems(items)
        this.items.addAll(items)
        this.items.sortBy { it.employeeName }
    }

    fun removeItems(items: List<TimeEntryEmployee>) {
        this.items.removeAll { it.employeeId.partOf(items.map { it.employeeId }.toIntArray()) }
    }

    fun clear() {
        this.items.clear()
        notifyDataSetChanged()
    }

    private fun checkAllSelected() = bus.post(OnApproveCheckboxCheckedChanged(fragmentType, ArrayList(items).filter { it.canBeSelected }.all { it.selected }))

    fun anySelected() = items.any { it.canBeSelected && it.selected }

    fun canAnyBeSelected() = items.any { it.canBeSelected }

    fun selectDeselectAll(select: Boolean) {
        items.forEach { it.selected = if (it.canBeSelected) select else false }
        notifyDataSetChanged()
    }

    fun selectedItems() = ArrayList(items).filter { it.selected }

    fun isNotEmpty() = items.isNotEmpty()

    inner class TimeEntryEmployeeHolder(v: View) : RecyclerView.ViewHolder(v)
}
