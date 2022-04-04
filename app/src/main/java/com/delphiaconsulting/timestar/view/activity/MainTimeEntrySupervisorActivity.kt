package com.delphiaconsulting.timestar.view.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.core.content.ContextCompat
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.action.creators.TimeEntryActionsCreator
import com.delphiaconsulting.timestar.event.*
import com.delphiaconsulting.timestar.net.analytics.AnalyticsCategories.TIME_MANAGEMENT
import com.delphiaconsulting.timestar.store.TimeEntryStore
import com.delphiaconsulting.timestar.util.StringUtil.setSpanBetweenTokens
import com.delphiaconsulting.timestar.view.common.TimeEntryEmployee
import com.delphiaconsulting.timestar.view.common.TimeEntryPayGroup
import com.delphiaconsulting.timestar.view.extension.snack
import com.delphiaconsulting.timestar.view.fragment.TimeEntryEmployeeListFragment
import kotlinx.android.synthetic.main.activity_time_entry_supervisor_content.*
import kotlinx.android.synthetic.main.content_selector_spinner.*
import org.greenrobot.eventbus.Subscribe
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class MainTimeEntrySupervisorActivity : BaseActivity() {

    companion object {
        private const val PAGES_COUNT = 2
        private const val EMPLOYEE_TIME_ENTRY_REQUEST_CODE = 0

        fun getCallingIntent(context: Context): Intent = Intent(context, TimeEntrySupervisorActivity::class.java)
    }

    @Inject lateinit var timeEntryActionsCreator: TimeEntryActionsCreator
    @Inject lateinit var timeEntryStore: TimeEntryStore

    private var loadingPayGroup: TimeEntryPayGroup? = null
    private var loadSubscription: Subscription? = null
    private var progressUpdateSubscription: Subscription? = null
    private var dismissSnackbarSubscription: Subscription? = null
    protected var snackbar: Snackbar? = null
    private var approvingUpdate: Boolean = false
    private var clickedEmployee: TimeEntryEmployee? = null
    private var trackPayGroupChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        progressUpdateSubscription = timeEntryStore.onEmployeeLoadingProgressUpdateSubject.observeOn(AndroidSchedulers.mainThread()).subscribe { onEvent(it) }
        setContentView(R.layout.activity_time_entry_supervisor)
        showProgressBar(true)
        setupViewPager()
        timeEntryActionsCreator.getEmployeeList()
    }


    override fun onDestroy() {
        progressUpdateSubscription?.unsubscribe()
        loadSubscription?.unsubscribe()
        super.onDestroy()
    }

    override fun registerInBus() = bus.register(this)

    override fun unregisterFromBus() = bus.unregister(this)

    override val titleResource: Int
        get() = R.string.activity_time_entry_supervisor_title

    override val trackerScreen: String
        get() = "Time Management"

    private fun setupViewPager() {
        viewPager.adapter = ViewPagerAdapter()
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                tracker.trackEvent(TIME_MANAGEMENT, "Tab", if (tab.position == 0) "Needs Approval" else "Approved")
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    @Subscribe
    fun onEvent(event: OnPayGroupsAndEmployeesReceived) {
        showProgressBar(false)
        val adapter = object : ArrayAdapter<TimeEntryPayGroup>(baseContext, android.R.layout.simple_list_item_1, event.payGroups) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getView(position, convertView, parent) as TextView
                val text = setSpanBetweenTokens(getString(R.string.supervisor_spinner_view_text, getItem(position)?.name, getItem(position)?.formattedPayPeriod), "##", StyleSpan(Typeface.BOLD))
                textView.text = setSpanBetweenTokens(text, "@@", RelativeSizeSpan(.86f))
                textView.setTextColor(ContextCompat.getColor(baseContext, R.color.text_white))
                return textView
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getDropDownView(position, convertView, parent) as TextView
                textView.text = getString(R.string.supervisor_spinner_dropdown_text, getItem(position)?.name, getItem(position)?.employees?.size)
                return textView
            }
        }
        val onItemSelectedAction: (Int) -> Unit = { position -> onPayGroupSelected(event.payGroups[position]) }
        setupContentSelectorSpinner(adapter, onItemSelectedAction, hideIfSingleItem = false)
        if (event.payGroups.size == 1) {
            contentSelectorSpinner.isEnabled = false
            return
        }
    }

    private fun onPayGroupSelected(payGroup: TimeEntryPayGroup) {
        loadingPayGroup = payGroup
        bus.post(OnTimeEntryPayGroupSelected(payGroup))
        loadSubscription?.unsubscribe()
        dismissSnackbarSubscription?.unsubscribe()
        updateSnackbar(getString(R.string.loading_x_out_of_y_text, 0, payGroup.employees.size))
        loadSubscription = timeEntryActionsCreator.loadEmployees(payGroup.employees, payGroup.id)
        if (!trackPayGroupChange) {
            trackPayGroupChange = true
            return
        }
        val dateParser = DateTimeFormat.forPattern("MM/dd/yyyy")
        tracker.trackEvent(TIME_MANAGEMENT, "Change Paygroup", Days.daysBetween(dateParser.parseDateTime(payGroup.startDate), dateParser.parseDateTime(payGroup.stopDate)).days.toString())
    }

    fun onEvent(event: OnEmployeeLoadingProgressUpdate) {
        if ((event.updateTextRes == R.string.loading_x_out_of_y_text && approvingUpdate)) return
        approvingUpdate = event.updateTextRes == R.string.approving_x_out_of_y_text || event.updateTextRes == R.string.unapproving_x_out_of_y_text
        updateSnackbar(getString(event.updateTextRes, event.loaded, event.total))
        dismissSnackbarSubscription?.unsubscribe()
        if (event.loaded == event.total) {
            dismissSnackbarSubscription = Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                snackbar?.dismiss()
                approvingUpdate = false
            }
        }
    }

    private fun updateSnackbar(snackMessage: String) {
        if (snackbar?.isShown == true) {
            snackbar?.setText(snackMessage)
            return
        }
        coordinatorLayout.snack(snackMessage, Snackbar.LENGTH_INDEFINITE) {
            snackbar = this
            val progressBar = LayoutInflater.from(this@MainTimeEntrySupervisorActivity).inflate(R.layout.snack_progress_bar, null)
            (snackbar?.view as Snackbar.SnackbarLayout).addView(progressBar)
        }
    }

    @Subscribe
    fun onEvent(event: OnTimeEntryDataError) {
        if (clickedEmployee != null) return
        showProgressBar(false)
        coordinatorLayout.snack(event.message ?: getString(R.string.error_retrieving_info_text)) {}
    }

    @Subscribe
    fun onEvent(event: OnTimeEntryEmployeeClicked) {
        clickedEmployee = event.employee
        clickedEmployee?.let { startActivityForResult(MainTimeEntryActivity.getCallingIntent(this, it.employeeId, it.error), EMPLOYEE_TIME_ENTRY_REQUEST_CODE) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != EMPLOYEE_TIME_ENTRY_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        clickedEmployee?.let {
            val statusChanged = data?.getBooleanExtra(MainTimeEntryActivity.EMPLOYEE_MASK_EXTRA, false) ?: false
            val error = data?.getStringExtra(MainTimeEntryActivity.EMPLOYEE_ERROR_EXTRA)
            if (it.employeeId != data?.getIntExtra(MainTimeEntryActivity.EMPLOYEE_ID_EXTRA, -1) || (!statusChanged && it.error == error)) return@let
            showProgressBar(true)
            loadingPayGroup?.let { payGroup -> timeEntryActionsCreator.loadEmployee(it, payGroup.id, error ?: "") }
        }
        clickedEmployee = null
    }

    private inner class ViewPagerAdapter internal constructor() : FragmentStatePagerAdapter(supportFragmentManager) {
        private val titles: Array<String> = resources.getStringArray(R.array.time_entry_supervisor_tabs)

        override fun getItem(position: Int) = TimeEntryEmployeeListFragment.newInstance(position)

        override fun getPageTitle(fragmentType: Int) = titles[fragmentType]

        override fun getCount() = PAGES_COUNT
    }
}
