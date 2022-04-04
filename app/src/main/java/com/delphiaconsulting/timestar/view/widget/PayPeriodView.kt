package com.delphiaconsulting.timestar.view.widget

import android.content.Context
import androidx.appcompat.widget.ListPopupWindow
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.delphiaconsulting.timestar.App
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.action.creators.TimeEntryActionsCreator
import com.delphiaconsulting.timestar.event.OnPayPeriodListReceived
import com.delphiaconsulting.timestar.event.OnPayPeriodSelected
import com.delphiaconsulting.timestar.view.common.AdapterItem
import com.delphiaconsulting.timestar.view.common.PayPeriodItem
import kotlinx.android.synthetic.main.pay_period_item.view.*
import kotlinx.android.synthetic.main.pay_period_section_item.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject


/**
 * Created by dxsier on 2/20/18.
 */
class PayPeriodView(context: Context, attributeSet: AttributeSet) : TextView(context, attributeSet) {

    @Inject lateinit var bus: EventBus
    @Inject lateinit var actionsCreator: TimeEntryActionsCreator

    init {
        (context.applicationContext as App).component.inject(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bus.register(this)
    }

    override fun onDetachedFromWindow() {
        bus.unregister(this)
        super.onDetachedFromWindow()
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnPayPeriodListReceived) {
        val currentPayPeriod = event.payPeriods
                .asSequence()
                .filter { it is PayPeriodItem }
                .map { it as PayPeriodItem }
                .firstOrNull { it.payPeriod.date.idx == event.currentPeriodId }
        currentPayPeriod?.let { setSelectedPayPeriod(it) }
        val payPeriods = event.payPeriods.asSequence().filter { !it.isSection }

        setOnClickListener {
            val popupWindow = ListPopupWindow(context)
            popupWindow.anchorView = it
            popupWindow.setAdapter(PayPeriodAdapter(context, event.payPeriods, tag.toString()))
            popupWindow.setOnItemClickListener { adapterView, _, position, _ ->
                val payPeriodItem = (adapterView?.getItemAtPosition(position) as PayPeriodItem)
                val positionsAwayFromCurrent = currentPayPeriod?.let { payPeriods.indexOf(payPeriodItem) - payPeriods.indexOf(it) } ?: 0
                onPayPeriodSelected(payPeriodItem, positionsAwayFromCurrent)
                popupWindow.dismiss()
            }
            popupWindow.width = resources.getDimension(R.dimen.pay_period_popup_width).toInt()
            popupWindow.show()
            scrollToSelected(popupWindow, event.payPeriods)
        }
    }

    private fun onPayPeriodSelected(payPeriodItem: PayPeriodItem, positionsAwayFromCurrent: Int) {
        if (payPeriodItem.payPeriod.date.idx == tag) return
        setSelectedPayPeriod(payPeriodItem, positionsAwayFromCurrent)
    }

    private fun setSelectedPayPeriod(payPeriodItem: PayPeriodItem, positionsAwayFromCurrent: Int = 0) {
        text = payPeriodItem.payPeriod.date.formattedPayPeriod
        tag = payPeriodItem.payPeriod.date.idx
        bus.post(OnPayPeriodSelected(payPeriodItem, positionsAwayFromCurrent))
    }

    private fun scrollToSelected(popupWindow: ListPopupWindow, payPeriods: List<AdapterItem>) {
        for (payPeriod in payPeriods) {
            if (payPeriod is PayPeriodItem && payPeriod.payPeriod.date.idx == tag.toString()) {
                popupWindow.setSelection(payPeriods.indexOf(payPeriod) - 1)
                break
            }
        }
    }

    private class PayPeriodAdapter(val context: Context, val items: List<AdapterItem>, val selectedPayPeriodId: String) : BaseAdapter() {

        companion object {
            const val PAY_PERIOD_SECTION_ITEM = 0
            const val PAY_PERIOD_ITEM = 1
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: getInflatedLayout(position)
            if (getItemViewType(position) == PAY_PERIOD_SECTION_ITEM) {
                view.sectionNameText.text = getItem(position).sectionName
                return view
            }
            val item = getItem(position) as PayPeriodItem
            view.payPeriodText.text = item.payPeriod.date.formattedPayPeriod
            view.currentPeriodText.visibility = if (item.current) View.VISIBLE else View.GONE
            view.selectedImage.visibility = if (selectedPayPeriodId == item.payPeriod.date.idx) View.VISIBLE else View.INVISIBLE
            return view
        }

        private fun getInflatedLayout(position: Int): View {
            val layoutRes = if (getItemViewType(position) == PAY_PERIOD_SECTION_ITEM) R.layout.pay_period_section_item else R.layout.pay_period_item
            return LayoutInflater.from(context).inflate(layoutRes, null)
        }

        override fun isEnabled(position: Int) = !items[position].isSection

        override fun getItemViewType(position: Int) = if (items[position].isSection) PAY_PERIOD_SECTION_ITEM else PAY_PERIOD_ITEM

        override fun getViewTypeCount() = 2

        override fun getItem(position: Int): AdapterItem = items[position]

        override fun getItemId(position: Int) = items[position].id

        override fun getCount() = items.size
    }
}