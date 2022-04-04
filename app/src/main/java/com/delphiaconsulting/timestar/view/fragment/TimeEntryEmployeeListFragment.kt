package com.delphiaconsulting.timestar.view.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.avast.android.dialogs.fragment.SimpleDialogFragment
import com.avast.android.dialogs.iface.ISimpleDialogListener
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.action.creators.TimeEntryActionsCreator
import com.delphiaconsulting.timestar.event.*
import com.delphiaconsulting.timestar.net.analytics.AnalyticsCategories.TIME_MANAGEMENT
import com.delphiaconsulting.timestar.net.analytics.Tracker
import com.delphiaconsulting.timestar.net.gson.TimeApproveRequest
import com.delphiaconsulting.timestar.store.TimeEntryStore
import com.delphiaconsulting.timestar.util.AppUtil
import com.delphiaconsulting.timestar.util.TimeEntryUtil.APPROVE_ACTION_KEY
import com.delphiaconsulting.timestar.util.TimeEntryUtil.NEEDS_APPROVAL_MASK_STATUS_ARRAY
import com.delphiaconsulting.timestar.util.TimeEntryUtil.NO_ACTION_REQUIRED_MASK_STATUS_ARRAY
import com.delphiaconsulting.timestar.util.TimeEntryUtil.SUPERVISOR_ACTION_KEY
import com.delphiaconsulting.timestar.util.TimeEntryUtil.UNAPPROVE_ACTION_KEY
import com.delphiaconsulting.timestar.view.adapter.TimeEntryEmployeeAdapter
import com.delphiaconsulting.timestar.view.common.TimeEntryEmployee
import com.delphiaconsulting.timestar.view.common.TimeEntryPayGroup
import com.delphiaconsulting.timestar.view.extension.partOf
import com.delphiaconsulting.timestar.view.widget.MatrixLayoutManager
import kotlinx.android.synthetic.main.fragment_time_entry_employee_list.*
import kotlinx.android.synthetic.main.view_time_entry_employee_header.*
import kotlinx.android.synthetic.main.widget_footnote.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TimeEntryEmployeeListFragment : BaseFragment(), ISimpleDialogListener {

    companion object {
        private const val NEEDS_APPROVAL_FRAGMENT_TYPE = 0
        private const val FRAGMENT_TYPE_EXTRA = "FRAGMENT_TYPE_EXTRA"
        private const val APPROVAL_CONFIRMATION_REQUEST_CODE = 0
        private const val LIST = 0
        private const val FOOTNOTE = 1
        private const val NONE = 2

        fun newInstance(fragmentType: Int): TimeEntryEmployeeListFragment {
            val bundle = Bundle()
            bundle.putInt(FRAGMENT_TYPE_EXTRA, fragmentType)
            val fragment = TimeEntryEmployeeListFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject lateinit var appUtil: AppUtil
    @Inject lateinit var bus: EventBus
    @Inject lateinit var tracker: Tracker
    @Inject lateinit var actionsCreator: TimeEntryActionsCreator
    @Inject lateinit var store: TimeEntryStore

    private var adapter: TimeEntryEmployeeAdapter? = null
    private var fragmentType: Int? = null
    private var selectedPayGroup: TimeEntryPayGroup? = null
    private var subscriptions: MutableList<Subscription> = ArrayList()
    private var showViewSubscription: Subscription? = null
    private var selectedEmployees: List<TimeEntryEmployee>? = null
    private var empAppDisabled: Boolean? = null
    private var dollarsDisabled: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_time_entry_employee_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
        subscriptions.add(store.onEmployeeLoadingProgressUpdateSubject.observeOn(AndroidSchedulers.mainThread()).subscribe { onEvent(it) })
        subscriptions.add(store.onEmployeeBatchProcessedSubject.observeOn(AndroidSchedulers.mainThread()).subscribe { onEvent(it) })
        footnoteText.setText(R.string.no_employee_information_found_text)
        fragmentType = arguments?.getInt(FRAGMENT_TYPE_EXTRA)
        setupSelectAllCheckboxListener()
        approvalActionButton.setText(if (fragmentType == NEEDS_APPROVAL_FRAGMENT_TYPE) R.string.approve_button_text else R.string.unapprove_button_text)
        approvalActionButton.setOnClickListener { showApprovalConfirmationDialog() }
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        subscriptions.forEach { it.unsubscribe() }
        showViewSubscription?.unsubscribe()
        super.onDestroy()
    }

    @Subscribe
    fun onEvent(event: OnSupervisorAccessFlagsReceived) {
        if ((fragmentType == NEEDS_APPROVAL_FRAGMENT_TYPE && !event.canApprove) || (fragmentType != NEEDS_APPROVAL_FRAGMENT_TYPE && !event.canUnapprove)) {
            approvalActionButton.visibility = View.GONE
            selectAllCheckbox.visibility = View.INVISIBLE
        }
        empAppDisabled = event.empAppDisabled
        dollarsDisabled = event.dollarsDisabled
        context?.let { setupHeaderMeasures(event.empAppDisabled, event.dollarsDisabled, it) }
    }

    private fun setupHeaderMeasures(empAppDisabled: Boolean, dollarsDisabled: Boolean, context: Context) {
        val screenWidth = context.resources.displayMetrics.widthPixels - context.resources.getDimension(R.dimen.full_and_half_margin)
        var widthArray: MutableList<Int> = ArrayList()
        widthArray.addAll(context.resources.let {
            arrayOf(it.getDimension(R.dimen.sup_list_checkbox_width), it.getDimension(if (empAppDisabled) R.dimen.sup_list_name_item_width else R.dimen.sup_list_name_header_width), it.getDimension(if (empAppDisabled) R.dimen.sup_list_zero_width else R.dimen.sup_list_emp_app_header_width),
                    it.getDimension(if (empAppDisabled) R.dimen.sup_list_total_item_width else R.dimen.sup_list_total_header_width), it.getDimension(if (dollarsDisabled) R.dimen.sup_list_zero_width else R.dimen.sup_list_dollars_width))
        }.map { it.toInt() })
        val headerWidth = widthArray.reduce { accumulator, width -> accumulator + width }.toInt()
        if (screenWidth > headerWidth) {
            widthArray = ArrayList(widthArray.map { Math.round(it * screenWidth / headerWidth) })
        }
        val columnViews = arrayOf(selectAllCheckbox, nameHeaderText, empAppHeaderText, totalHeaderText, dollarsHeaderText)
        for (i in 0 until widthArray.size) {
            columnViews[i].width = widthArray[i]
        }
    }

    @Subscribe
    fun onEvent(event: OnTimeEntryPayGroupSelected) {
        selectedPayGroup = event.payGroup
        adapter?.clear()
        showViewSubscription?.unsubscribe()
        showView(NONE)
    }

    @Subscribe
    fun onEvent(event: OnApproveCheckboxCheckedChanged) {
        if (event.fragmentType != fragmentType) return
        setSelectAllCheckboxChecked(event.allSelected)
        enableApprovalButton()
    }

    private fun enableApprovalButton() {
        approvalActionButton.isEnabled = adapter?.anySelected() == true
    }

    private fun setSelectAllCheckboxChecked(checked: Boolean) {
        selectAllCheckbox.setOnCheckedChangeListener(null)
        selectAllCheckbox.isChecked = checked
        setupSelectAllCheckboxListener()
    }

    private fun setupSelectAllCheckboxListener() = selectAllCheckbox.setOnCheckedChangeListener { _, checked ->
        adapter?.selectDeselectAll(checked)
        enableApprovalButton()
    }

    fun onEvent(event: OnEmployeeLoadingProgressUpdate) {
        showViewSubscription?.unsubscribe()
        if (selectedPayGroup?.id != event.loadingPayGroupId) return
        if (event.loaded < event.total) {
            showView(if (adapter?.isNotEmpty() == true) LIST else NONE)
            return
        }
        showViewSubscription = Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            showView(if (adapter?.isNotEmpty() == true) LIST else FOOTNOTE)
        }
    }

    fun onEvent(event: OnEmployeeBatchProcessed) {
        if (selectedPayGroup?.id != event.loadingPayGroupId) return
        setupRecyclerView(event.employeeBatch)
        if (event.approving) {
            showProgressBar(false)
            val presentedErrors = event.employeeBatch.any { it.error != null }
            showResultDialog(presentedErrors)
            tracker.trackEvent(TIME_MANAGEMENT, "Type", if (fragmentType == NEEDS_APPROVAL_FRAGMENT_TYPE) "SUP Approve" else "SUP Unapprove")
            tracker.trackEvent(TIME_MANAGEMENT, "Submit", if (presentedErrors) "SuccessWithFail" else "Success")
            tracker.trackEvent(TIME_MANAGEMENT, "Employees", event.employeeBatch.size.toString())
            selectedPayGroup?.let {
                val dateParser = DateTimeFormat.forPattern("MM/dd/yyyy")
                tracker.trackEvent(TIME_MANAGEMENT, "Paygroup", Days.daysBetween(dateParser.parseDateTime(it.startDate), dateParser.parseDateTime(it.stopDate)).days.toString())
            }
        }
    }

    private fun setupRecyclerView(items: List<TimeEntryEmployee>) = context?.let { context ->
        adapter = adapter ?: TimeEntryEmployeeAdapter(bus, fragmentType, context, childFragmentManager, empAppDisabled == true, dollarsDisabled == true)
        if (recyclerView.adapter == null) {
            recyclerView.layoutManager = MatrixLayoutManager(adapter).setHorizontalScrollListener(object : MatrixLayoutManager.HorizontalScrollListener {
                override fun scrollHorizontallyBy(dx: Int) = itemContainer.offsetLeftAndRight(dx)

                override fun setLeftCoordinate(left: Int) = itemContainer.offsetLeftAndRight(left - itemContainer.left)
            })
            recyclerView.adapter = adapter
        }
        if (items.isNotEmpty()) {
            adapter?.insertItems(items.filter { it.maskStatus.partOf(if (fragmentType == NEEDS_APPROVAL_FRAGMENT_TYPE) NEEDS_APPROVAL_MASK_STATUS_ARRAY else NO_ACTION_REQUIRED_MASK_STATUS_ARRAY) })
            adapter?.removeItems(items.filter { it.maskStatus.partOf(if (fragmentType != NEEDS_APPROVAL_FRAGMENT_TYPE) NEEDS_APPROVAL_MASK_STATUS_ARRAY else NO_ACTION_REQUIRED_MASK_STATUS_ARRAY) })
            adapter?.notifyDataSetChanged()
            setSelectAllCheckboxChecked(false)
            selectAllCheckbox.isEnabled = adapter?.canAnyBeSelected() == true
            enableApprovalButton()
        }
    }

    private fun showView(show: Int) {
        listContainer.visibility = if (show == LIST) View.VISIBLE else View.GONE
        footnoteText.visibility = if (show == FOOTNOTE) View.VISIBLE else View.GONE
    }

    private fun showResultDialog(presentedErrors: Boolean) {
        if (selectedEmployees == null) return
        selectedEmployees = null
        val message = getString(if (presentedErrors) R.string.approval_action_error_result_text else R.string.approval_action_success_result_text)
        SimpleDialogFragment.createBuilder(context, childFragmentManager)
                .setMessage(message)
                .setPositiveButtonText(R.string.ok_btn_text)
                .show()
    }

    private fun showApprovalConfirmationDialog() {
        selectedEmployees = adapter?.selectedItems()
        selectedEmployees?.let {
            val message = if (it.size == 1) {
                getString(if (fragmentType == NEEDS_APPROVAL_FRAGMENT_TYPE) R.string.approve_time_for_single_text else R.string.unapprove_time_for_single_text, it[0].employeeName)
            } else {
                getString(if (fragmentType == NEEDS_APPROVAL_FRAGMENT_TYPE) R.string.approve_time_for_multiple_text else R.string.unapprove_time_for_multiple_text, it.size)
            }
            SimpleDialogFragment.createBuilder(context, fragmentManager)
                    .setMessage(message)
                    .setPositiveButtonText(R.string.yes_btn_text)
                    .setNegativeButtonText(R.string.no_btn_text)
                    .setTargetFragment(this, APPROVAL_CONFIRMATION_REQUEST_CODE)
                    .show()
        }
    }

    override fun onPositiveButtonClicked(requestCode: Int) {
        if (requestCode != APPROVAL_CONFIRMATION_REQUEST_CODE) return
        selectedEmployees?.let { employees ->
            val approvalList = employees.map { TimeApproveRequest.ApprovalRequest(if (fragmentType == NEEDS_APPROVAL_FRAGMENT_TYPE) APPROVE_ACTION_KEY else UNAPPROVE_ACTION_KEY, SUPERVISOR_ACTION_KEY, it.employeeId) }
            selectedPayGroup?.let {
                showProgressBar(true)
                val updateTextRes = if (fragmentType == NEEDS_APPROVAL_FRAGMENT_TYPE) R.string.approving_x_out_of_y_text else R.string.unapproving_x_out_of_y_text
                bus.post(OnEmployeeLoadingProgressUpdate(updateTextRes, 0, employees.size, it.id))
                subscriptions.add(actionsCreator.massApproveTime(TimeApproveRequest(it.payPeriodIdx, approvalList), employees, it.id, updateTextRes))
            }
        }
    }

    override fun onNeutralButtonClicked(requestCode: Int) {}

    override fun onNegativeButtonClicked(requestCode: Int) {}
}
