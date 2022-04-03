package com.insperity.escmobile.view.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import android.view.LayoutInflater
import android.view.View
import com.avast.android.dialogs.fragment.SimpleDialogFragment
import com.insperity.escmobile.R
import com.insperity.escmobile.action.creators.TimeEntryActionsCreator
import com.insperity.escmobile.event.*
import com.insperity.escmobile.net.analytics.AnalyticsCategories.TIMESHEET
import com.insperity.escmobile.net.analytics.AnalyticsCategories.TIMESHEET_SUP
import com.insperity.escmobile.net.gson.TimeApproveRequest
import com.insperity.escmobile.store.TimeEntryStore
import com.insperity.escmobile.util.TimeEntryUtil.APPROVE_ACTION_KEY
import com.insperity.escmobile.util.TimeEntryUtil.EMPLOYEE_ACTION_KEY
import com.insperity.escmobile.util.TimeEntryUtil.EMPLOYEE_APPROVED_STATUS_ARRAY
import com.insperity.escmobile.util.TimeEntryUtil.EMPLOYEE_CAN_CHANGE_STATUS_ARRAY
import com.insperity.escmobile.util.TimeEntryUtil.SUPERVISOR_ACTION_KEY
import com.insperity.escmobile.util.TimeEntryUtil.SUPERVISOR_APPROVED_STATUS_ARRAY
import com.insperity.escmobile.util.TimeEntryUtil.SUPERVISOR_CAN_CHANGE_STATUS_ARRAY
import com.insperity.escmobile.util.TimeEntryUtil.UNAPPROVE_ACTION_KEY
import com.insperity.escmobile.view.common.PayPeriodItem
import com.insperity.escmobile.view.extension.snack
import com.insperity.escmobile.view.extension.visible
import com.insperity.escmobile.view.fragment.*
import kotlinx.android.synthetic.main.activity_time_entry_content.*
import kotlinx.android.synthetic.main.card_view_time_review.*
import kotlinx.android.synthetic.main.view_notification_badge.*
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import java.util.*
import javax.inject.Inject

abstract class MainTimeEntryActivity : BaseActivity() {

    companion object {
        const val EMPLOYEE_ID_EXTRA = "EMPLOYEE_ID_EXTRA"
        const val EMPLOYEE_MASK_EXTRA = "EMPLOYEE_MASK_EXTRA"
        const val EMPLOYEE_ERROR_EXTRA = "EMPLOYEE_ERROR_EXTRA"
        private const val APPROVE_EMPLOYEE_CONFIRMATION_REQUEST_CODE = 10900
        private const val APPROVE_SUPERVISOR_CONFIRMATION_REQUEST_CODE = 10901
        private const val UNAPPROVE_EMPLOYEE_CONFIRMATION_REQUEST_CODE = 10902
        private const val UNAPPROVE_SUPERVISOR_CONFIRMATION_REQUEST_CODE = 10903

        fun getCallingIntent(context: Context, employeeId: Int = -1, employeeError: String? = null, clazz: Class<out MainTimeEntryActivity> = TimeEntryActivity::class.java): Intent =
                Intent(context, clazz).putExtra(EMPLOYEE_ID_EXTRA, employeeId).putExtra(EMPLOYEE_ERROR_EXTRA, employeeError)
    }

    @Inject lateinit var timeEntryActionsCreator: TimeEntryActionsCreator
    @Inject lateinit var timeEntryStore: TimeEntryStore

    private var payPeriodItem: PayPeriodItem? = null
    private var employeeId = -1
    private var lastAccessEvent: OnBottomTabAccessReceived? = null
    private var previousMaskStatus: Int? = null
    private var currentMaskStatus: Int? = null
    private var approvalTypeTrackLabel: String? = null
    private var currentPayPeriodError: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        setContentView(R.layout.activity_time_entry)
        showProgressBar(true)
        employeeId = intent.getIntExtra(EMPLOYEE_ID_EXTRA, -1)
        timeEntryActionsCreator.getPayPeriods(if (employeeId != -1) employeeId else null)
        currentPayPeriodError = intent.getStringExtra(EMPLOYEE_ERROR_EXTRA)
        showApprovalErrorMessage(error = currentPayPeriodError)
    }

    override fun registerInBus() = bus.register(this)

    override fun unregisterFromBus() = bus.unregister(this)

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().putExtra(EMPLOYEE_ID_EXTRA, employeeId).putExtra(EMPLOYEE_MASK_EXTRA, previousMaskStatus != currentMaskStatus).putExtra(EMPLOYEE_ERROR_EXTRA, currentPayPeriodError))
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        bus.removeAllStickyEvents()
    }

    override val titleResource: Int
        get() = R.string.activity_time_review_title

    override val trackerScreen: String
        get() = "Timesheet"

    @Subscribe
    fun onEvent(event: OnEmployeeInfoReceived) {
        employeeId = event.employeeId
        nameText.text = event.name
        idText.text = event.id
        timeReviewCard.visibility = View.VISIBLE
        setupFragmentContent()
    }

    @Subscribe
    fun onEvent(event: OnTimeEntryTotalsReceived) {
        totalHoursAmountText.text = event.totalHours
        hoursAmountText.text = event.hours
        punchesAmountText.text = event.punches
        totalsContainer.visibility = View.VISIBLE
        showProgressBar(false)
    }

    @Subscribe
    fun onEvent(event: OnApprovalMaskStatusReceived) {
        if (previousMaskStatus == null) {
            previousMaskStatus = event.maskStatus
        }
        currentMaskStatus?.let {
            if (it != event.maskStatus) {
                tracker.trackEvent(getTrackerCategory(), "ApprovalStatus", "$it:${event.maskStatus}")
            }
        }
        currentMaskStatus = event.maskStatus
        val employeeApproved = EMPLOYEE_APPROVED_STATUS_ARRAY.any { it == currentMaskStatus }
        val employeeCanChange = EMPLOYEE_CAN_CHANGE_STATUS_ARRAY.any { it == currentMaskStatus }
        employeeApprovedContainer.visibility = if (employeeApproved) View.VISIBLE else View.GONE
        employeeUnapproveLink.visibility = if (employeeApproved && employeeCanChange) View.VISIBLE else View.GONE
        employeeUnapproveLink.setOnClickListener { onTimeApprovalActionClickListener(UNAPPROVE_EMPLOYEE_CONFIRMATION_REQUEST_CODE) }

        val supervisorApproved = SUPERVISOR_APPROVED_STATUS_ARRAY.any { it == currentMaskStatus }
        val supervisorCanChange = SUPERVISOR_CAN_CHANGE_STATUS_ARRAY.any { it == currentMaskStatus }
        supervisorApprovedContainer.visibility = if (supervisorApproved) View.VISIBLE else View.GONE
        supervisorUnapproveLink.visibility = if (supervisorApproved && supervisorCanChange) View.VISIBLE else View.GONE
        supervisorUnapproveLink.setOnClickListener { onTimeApprovalActionClickListener(UNAPPROVE_SUPERVISOR_CONFIRMATION_REQUEST_CODE) }

        employeeApproveButton.visibility = if (!employeeApproved && employeeCanChange) View.VISIBLE else View.GONE
        employeeApproveButton.setOnClickListener { onTimeApprovalActionClickListener(APPROVE_EMPLOYEE_CONFIRMATION_REQUEST_CODE) }
        supervisorApproveButton.visibility = if (!supervisorApproved && supervisorCanChange) View.VISIBLE else View.GONE
        supervisorApproveButton.setOnClickListener { onTimeApprovalActionClickListener(APPROVE_SUPERVISOR_CONFIRMATION_REQUEST_CODE) }
        actionsContainer.visibility = if ((!employeeApproved && employeeCanChange) || (!supervisorApproved && supervisorCanChange)) View.VISIBLE else View.GONE
    }

    private fun onTimeApprovalActionClickListener(requestCode: Int) = SimpleDialogFragment.createBuilder(this, supportFragmentManager)
            .setMessage(getString(if (requestCode == APPROVE_EMPLOYEE_CONFIRMATION_REQUEST_CODE || requestCode == APPROVE_SUPERVISOR_CONFIRMATION_REQUEST_CODE) R.string.time_approve_confirmation_text else R.string.time_unapprove_confirmation_text, payPeriodText.text))
            .setPositiveButtonText(R.string.ok_btn_text)
            .setNegativeButtonText(R.string.cancel_btn_text)
            .setRequestCode(requestCode)
            .show()

    override fun onPositiveButtonClicked(requestCode: Int) {
        approvalTypeTrackLabel = getApprovalTypeLabel(requestCode)
        if (requestCode == APPROVE_EMPLOYEE_CONFIRMATION_REQUEST_CODE) {
            approveTime(TimeApproveRequest.ApprovalRequest(APPROVE_ACTION_KEY, EMPLOYEE_ACTION_KEY, employeeId))
            return
        }
        if (requestCode == APPROVE_SUPERVISOR_CONFIRMATION_REQUEST_CODE) {
            approveTime(TimeApproveRequest.ApprovalRequest(APPROVE_ACTION_KEY, SUPERVISOR_ACTION_KEY, employeeId))
            return
        }
        if (requestCode == UNAPPROVE_EMPLOYEE_CONFIRMATION_REQUEST_CODE) {
            approveTime(TimeApproveRequest.ApprovalRequest(UNAPPROVE_ACTION_KEY, EMPLOYEE_ACTION_KEY, employeeId))
            return
        }
        if (requestCode == UNAPPROVE_SUPERVISOR_CONFIRMATION_REQUEST_CODE) {
            approveTime(TimeApproveRequest.ApprovalRequest(UNAPPROVE_ACTION_KEY, SUPERVISOR_ACTION_KEY, employeeId))
            return
        }
        super.onPositiveButtonClicked(requestCode)
    }

    private fun getApprovalTypeLabel(requestCode: Int) = when (requestCode) {
        APPROVE_EMPLOYEE_CONFIRMATION_REQUEST_CODE -> "EE Approve"
        APPROVE_SUPERVISOR_CONFIRMATION_REQUEST_CODE -> "SUP Approve"
        UNAPPROVE_EMPLOYEE_CONFIRMATION_REQUEST_CODE -> "EE Unapprove"
        UNAPPROVE_SUPERVISOR_CONFIRMATION_REQUEST_CODE -> "SUP Unapprove"
        else -> null
    }

    private fun approveTime(approvalRequest: TimeApproveRequest.ApprovalRequest) {
        showProgressBar(true)
        val approvalList = ArrayList<TimeApproveRequest.ApprovalRequest>()
        approvalList.add(approvalRequest)
        showApprovalErrorMessage(show = false)
        payPeriodItem?.run { timeEntryActionsCreator.approveTime(employeeId, TimeApproveRequest(payPeriod.date.idx, approvalList)) }
    }

    @Subscribe
    fun onEvent(event: OnBottomTabAccessReceived) {
        if (lastAccessEvent?.access?.hours == event.access.hours && lastAccessEvent?.access?.punches == event.access.punches && lastAccessEvent?.access?.dollars == event.access.dollars) return
        lastAccessEvent = event
        bottomNavigationView.menu.clear()
        bottomNavigationView.inflateMenu(R.menu.menu_time_review)
        val bottomNavigationMenuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        val punchItemView = bottomNavigationMenuView.getChildAt(2) as BottomNavigationItemView
        if (event.access.punches) {
            val punchBadge = LayoutInflater.from(this).inflate(R.layout.view_notification_badge, bottomNavigationMenuView, false)
            punchItemView.addView(punchBadge)
        } else {
            bottomNavigationView.menu.removeItem(R.id.action_punches)
        }
        if (!event.access.hours) {
            bottomNavigationView.menu.removeItem(R.id.action_hours)
        }
        if (!event.access.dollars) {
            bottomNavigationView.menu.removeItem(R.id.action_dollars)
        }
    }

    private fun setupFragmentContent() {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.action_details -> TimeEntryDetailsFragment.newInstance()
                R.id.action_hours -> TimeEntryHoursFragment.newInstance()
                R.id.action_punches -> TimeEntryPunchFragment.newInstance()
                R.id.action_dollars -> TimeEntryDollarsFragment.newInstance()
                else -> TimeEntrySummaryFragment.newInstance()
            }
            replaceFragment(fragment)
            tracker.trackEvent(getTrackerCategory(), "Page", fragment.trackerPage)
            return@setOnNavigationItemSelectedListener true
        }
        replaceFragment(TimeEntrySummaryFragment.newInstance())
        bottomNavigationView.visibility = View.VISIBLE
    }

    private fun replaceFragment(fragment: TimeEntryBaseFragment) = supportFragmentManager.beginTransaction()
            .replace(R.id.contentContainer, fragment)
            .commit()

    @Subscribe
    fun onEvent(event: OnTimeEntryPunchesReceived) {
        val amount = event.items.count { it.highlighted }
        notificationContainer?.visibility = if (amount > 0) View.VISIBLE else View.INVISIBLE
        badgeText?.text = if (amount > 9) "9+" else "$amount"
    }

    @Subscribe
    fun onEvent(event: OnPayPeriodSelected) {
        showProgressBar(true)
        payPeriodItem?.run { showApprovalErrorMessage(show = false) }
        payPeriodItem = event.payPeriodItem
        payPeriodItem?.run { timeEntryActionsCreator.getTotalHours(payPeriod.date.idx, if (employeeId == -1) null else employeeId) }
        if (payPeriodItem?.current == true) {
            showApprovalErrorMessage(error = currentPayPeriodError)
        }
        if (event.positionsAwayFromCurrent == 0) return
        val positionsAwayFromCurrentIncludingSymbols = if (event.positionsAwayFromCurrent > 0) "+${event.positionsAwayFromCurrent}" else "${event.positionsAwayFromCurrent}"
        tracker.trackEvent(getTrackerCategory(), "Change Payperiod", "Current Payperiod $positionsAwayFromCurrentIncludingSymbols")
    }

    @Subscribe
    fun onEvent(event: OnApprovalTimeEntrySuccessful) {
        if (payPeriodItem?.current == true) {
            currentPayPeriodError = null
        }
        tracker.trackEvent(getTrackerCategory(), "Submit", "Success")
        payPeriodItem?.run { tracker.trackEvent(getTrackerCategory(), "Submit Day", Days.daysBetween(DateTime(), DateTimeFormat.forPattern("MM/dd/yyyy").parseDateTime(payPeriod.date.stopDate)).days.toString()) }
        approvalTypeTrackLabel?.let { tracker.trackEvent(getTrackerCategory(), "Type", it) }
    }

    @Subscribe
    fun onEvent(event: OnApprovalTimeEntryDataError) {
        showProgressBar(false)
        showApprovalErrorMessage(error = event.message)
        if (payPeriodItem?.current == true) {
            currentPayPeriodError = event.message
        }
        tracker.trackEvent(getTrackerCategory(), "Submit", "Fail:${event.message}")
    }

    private fun showApprovalErrorMessage(show: Boolean = true, error: String? = null) {
        errorMessageText.text = error ?: ""
        errorMessageText.visible = show && error != null && error.isNotEmpty()
    }

    @Subscribe
    fun onEvent(event: OnTimeEntryDataError) {
        showProgressBar(false)
        coordinatorLayout.snack(event.message ?: getString(R.string.error_retrieving_info_text)) {}
    }

    private fun getTrackerCategory() = if (intent.getIntExtra(EMPLOYEE_ID_EXTRA, -1) == -1) TIMESHEET else TIMESHEET_SUP
}
