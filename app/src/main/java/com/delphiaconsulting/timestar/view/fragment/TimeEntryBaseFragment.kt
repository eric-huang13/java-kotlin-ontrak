package com.delphiaconsulting.timestar.view.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.event.OnPayPeriodSelected
import com.delphiaconsulting.timestar.event.OnTimeEntryItemClicked
import com.delphiaconsulting.timestar.view.activity.MainTimeEntryActivity
import com.delphiaconsulting.timestar.view.activity.MainTimeEntryDetailActivity
import com.delphiaconsulting.timestar.view.adapter.TimeEntryDataAdapter
import com.delphiaconsulting.timestar.view.common.TimeEntryColumnData
import com.delphiaconsulting.timestar.view.common.TimeEntryRowData
import com.delphiaconsulting.timestar.view.widget.MatrixLayoutManager
import kotlinx.android.synthetic.main.fragment_time_entry_base.*
import kotlinx.android.synthetic.main.view_time_entry_lists_item.*
import kotlinx.android.synthetic.main.widget_footnote.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject


abstract class TimeEntryBaseFragment : BaseFragment() {

    companion object {
        private const val LIST = 0
        private const val FOOTNOTE = 1
        private const val NONE = 2
    }

    @Inject lateinit var bus: EventBus

    private var adapter: TimeEntryDataAdapter? = null

    abstract val trackerPage: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_time_entry_base, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
        footnoteText.setText(R.string.no_information_found_text)
    }

    override fun onStart() {
        super.onStart()
        registerInBus()
    }

    override fun onStop() {
        unregisterFromBus()
        super.onStop()
    }

    protected abstract fun registerInBus()

    protected abstract fun unregisterFromBus()

    protected fun setupRecyclerView(properties: List<TimeEntryColumnData>, items: List<TimeEntryRowData>) {
        if (properties.isEmpty()) {
            showView(FOOTNOTE)
            return
        }
        context?.let {
            setupHeaderMeasuresAndData(properties, it)
            val employeeId = activity?.intent?.getIntExtra(MainTimeEntryActivity.EMPLOYEE_ID_EXTRA, -1) ?: -1
            adapter = adapter ?: TimeEntryDataAdapter(it, bus, employeeId > -1)
            if (recyclerView.adapter == null) {
                recyclerView.layoutManager = MatrixLayoutManager(adapter).setHorizontalScrollListener(object : MatrixLayoutManager.HorizontalScrollListener {
                    override fun scrollHorizontallyBy(dx: Int) = itemContainer.offsetLeftAndRight(dx)

                    override fun setLeftCoordinate(left: Int) = itemContainer.offsetLeftAndRight(left - itemContainer.left)
                })
                recyclerView.adapter = adapter
            }
            adapter?.setData(properties, items)
            showView(if (items.isNotEmpty()) LIST else FOOTNOTE)
            showProgressBar(false)
        }
    }

    private fun setupHeaderMeasuresAndData(properties: List<TimeEntryColumnData>, context: Context) {
        val screenWidth = context.resources.displayMetrics.widthPixels - context.resources.getDimension(R.dimen.full_margin)
        val headerWidth = properties.asSequence().map { it.width }.reduce { accumulator, width -> accumulator + width }.toInt()
        if (screenWidth > headerWidth) {
            properties.forEach { it.finalWidth = Math.round(it.width.toFloat() * screenWidth / headerWidth) }
        }
        val columnViews = arrayOf(column0, column1, column2, column3, column4, column5, column6, column7, column8, column9)
        columnViews.forEach { it.visibility = View.GONE }
        for (i in 0 until properties.size) {
            columnViews[i].text = properties[i].text
            columnViews[i].width = properties[i].finalWidth
            columnViews[i].visibility = View.VISIBLE
            columnViews[i].setTypeface(columnViews[i].typeface, Typeface.BOLD)
            columnViews[i].gravity = properties[i].getGravity()
            columnViews[i].setPadding(0, 0, properties[i].getEndPadding(context), 0)
        }
    }

    private fun showView(show: Int) {
        listContainer.visibility = if (show == LIST) View.VISIBLE else View.GONE
        footnoteText.visibility = if (show == FOOTNOTE) View.VISIBLE else View.GONE
    }

    @Subscribe
    fun onEvent(event: OnTimeEntryItemClicked) {
        activity?.let {
            it.startActivity(MainTimeEntryDetailActivity.getCallingIntent(it))
            it.overridePendingTransition(R.anim.slide_up, R.anim.stay)
        }
    }

    @Subscribe
    fun onEvent(event: OnPayPeriodSelected) = showView(NONE)
}
