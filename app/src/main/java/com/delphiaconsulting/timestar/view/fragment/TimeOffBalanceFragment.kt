package com.delphiaconsulting.timestar.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.action.creators.TimeOffActionsCreator
import com.delphiaconsulting.timestar.event.OnTimeOffSummaryReceived
import com.delphiaconsulting.timestar.net.gson.TimeOffSummary
import com.delphiaconsulting.timestar.store.TimeOffStore
import com.delphiaconsulting.timestar.util.FormatHelper
import kotlinx.android.synthetic.main.fragment_time_off_balance.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import javax.inject.Inject

/**
 * Created by qktran on 1/22/17.
 */
class TimeOffBalanceFragment : BaseFragment() {

    @Inject lateinit var bus: EventBus
    @Inject lateinit var actionsCreator: TimeOffActionsCreator
    @Inject lateinit var store: TimeOffStore

    companion object {
        val TAG: String = TimeOffBalanceFragment::class.java.simpleName

        fun newInstance() = TimeOffBalanceFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_time_off_balance, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
        showProgressBar(true)
        actionsCreator.getSummary()
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    @Subscribe
    fun onEvent(event: OnTimeOffSummaryReceived) = setupPtoBalanceViewList(event.accrualBalances)

    private fun setupPtoBalanceViewList(accrualBalances: List<TimeOffSummary.AccrualBalance>) {
        var firstItem = true
        for (timeOffBalance in accrualBalances) {
            val view = generatePtoBalanceItem(timeOffBalance.accrualBucket.value, timeOffBalance.formattedBalance)
            contentContainer.addView(view)
            if (firstItem) {
                val divider = view.findViewById<View>(R.id.divider)
                divider.visibility = GONE
                firstItem = false
            }
        }
        accruedDateText.text = getString(R.string.accrued_as_of_date, FormatHelper.formattedDate(Date()))
        accruedDateText.visibility = VISIBLE
        contentContainer.visibility = VISIBLE
    }

    private fun generatePtoBalanceItem(accrualName: String, timeOffBalance: String): View {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_timeoff_balance_item, contentContainer, false)
        val titleTxt = view.findViewById<TextView>(R.id.balance_title)
        titleTxt.text = accrualName
        val balanceTxt = view.findViewById<TextView>(R.id.balance_amount)
        balanceTxt.text = timeOffBalance
        return view
    }
}
