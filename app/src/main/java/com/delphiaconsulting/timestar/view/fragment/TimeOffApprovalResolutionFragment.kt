package com.delphiaconsulting.timestar.view.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.Toast
import com.avast.android.dialogs.iface.IListDialogListener
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.action.creators.TimeOffActionsCreator
import com.delphiaconsulting.timestar.event.OnTimeOffRequestDetailsReceived
import com.delphiaconsulting.timestar.event.OnTimeOffResolutionSubmissionError
import com.delphiaconsulting.timestar.event.OnTimeOffResolutionSubmitted
import com.delphiaconsulting.timestar.net.analytics.AnalyticsCategories
import com.delphiaconsulting.timestar.net.analytics.FirebaseEvents
import com.delphiaconsulting.timestar.net.analytics.Tracker
import com.delphiaconsulting.timestar.net.gson.TimeOffRequestDetails
import com.delphiaconsulting.timestar.view.activity.MainTimeOffApprovalResolutionActivity
import com.delphiaconsulting.timestar.view.extension.snack
import kotlinx.android.synthetic.main.fragment_time_off_approval_resolution.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import rx.Observable
import javax.inject.Inject

/**
 * Created by dxsier on 08/03/17.
 */
class TimeOffApprovalResolutionFragment : BaseFragment(), IListDialogListener {

    @Inject lateinit var bus: EventBus
    @Inject lateinit var tracker: Tracker
    @Inject lateinit var inputMethodManager: InputMethodManager
    @Inject lateinit var actionsCreator: TimeOffActionsCreator

    private var listDialogFragment: ListDialogFragment? = null
    private var recipients: List<TimeOffRequestDetails.LabelValueAttr>? = null
    private var recipientsSelection: IntArray? = null
    private var respondObservable: Observable<Pair<String, List<TimeOffRequestDetails.LabelValueAttr>>>? = null
    private var approving: Boolean = true
    private var recipientSequence = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_time_off_approval_resolution, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
        activity?.let { approving = it.intent.getBooleanExtra(MainTimeOffApprovalResolutionActivity.APPROVING_FLAG_EXTRA, true) }
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }

    @Subscribe
    fun onEvent(event: OnTimeOffResolutionSubmitted) {
        showProgressBar(false)
        activity?.let {
            it.setResult(Activity.RESULT_OK)
            it.finish()
        }
        tracker.trackEvent(AnalyticsCategories.TIME_OFF_APPROVAL, "Submit", "Success")
    }

    @Subscribe
    fun onEvent(event: OnTimeOffResolutionSubmissionError) {
        showProgressBar(false)
        submitButton.snack(event.message ?: getString(R.string.time_off_approval_service_error_text)) {}
        tracker.trackEvent(AnalyticsCategories.TIME_OFF_APPROVAL, "Submit", "Fail")
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeOffRequestDetailsReceived) {
        respondObservable = Observable.just(Pair(event.requestDetails.content.request.requestId.value, event.requestDetails.referenceData.optionLists.responseCode[event.requestDetails.content.request.responseCode.optionListIndex]))
        submitButton.setOnClickListener { submitResolution() }
        cancelButton.setOnClickListener {
            activity?.let {
                it.setResult(Activity.RESULT_CANCELED)
                it.finish()
            }
        }
        setupRecipientsToSelectFrom(event.requestDetails)
        recipientSequence = event.requestDetails.content.request.recipientItemSequence.value
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun setupRecipientsToSelectFrom(requestDetails: TimeOffRequestDetails) {
        if (requestDetails.referenceData.optionLists.recipientUsers == null) {
            return
        }

        recipients = requestDetails.referenceData.optionLists.recipientUsers[requestDetails.content.request.recipientUsers.optionListIndex]
        if (recipients == null || recipients!!.isEmpty()) {
            return
        }

        recipientsHeaderText.visibility = View.VISIBLE
        recipientsText.visibility = View.VISIBLE
        recipientsSelection = kotlin.IntArray(recipients!!.size)
        if (recipients!!.size == 1) {
            recipientsText.text = recipients!![0].label
            recipientsSelection!![0] = 0
            return
        }

        val min = requestDetails.content.request.recipientUsers.rules.min
        val max = requestDetails.content.request.recipientUsers.rules.max
        if (min == recipients!!.size) {
            for (i in 0 until recipients!!.size) {
                recipientsSelection!![i] = i
            }
            recipientsText.text = String.format("%s%s", recipients!![0].label, String.format(" +%d", recipientsSelection!!.size - 1))
        }

        recipientsText.setOnClickListener { setupRecipientSelectionPopup(min, max) }
    }

    private fun setupRecipientSelectionPopup(min: Int, max: Int) {
        recipientsText.error = null
        Observable.from(recipients!!)
                .map { it.label }.toList()
                .map { it.toTypedArray() }
                .subscribe {
                    listDialogFragment = ListDialogFragment.createBuilder(context, fragmentManager)
                            .setDismissible(false)
                            .setChoiceMode(if (min == 1 && min == max) AbsListView.CHOICE_MODE_SINGLE else AbsListView.CHOICE_MODE_MULTIPLE)
                            .setTitle(R.string.time_off_select_recipient_hint_text)
                            .setItems(it)
                            .setCheckedItems(recipientsSelection)
                            .setConfirmButtonText(R.string.ok_btn_text)
                            .setCancelButtonText(R.string.cancel_btn_text)
                            .setTargetFragment(this, 0)
                            .setOnItemsSelectedListener { values, selectedPositions, _ ->
                                handleRecipientsSelection(if (values.isEmpty()) "" else values[0].toString(), selectedPositions, min, max)
                            }
                            .show()
                }
    }

    override fun onListItemSelected(value: CharSequence?, number: Int, requestCode: Int) {
        handleRecipientsSelection(value.toString(), IntArray(1, { number }), 1, 1)
    }

    private fun handleRecipientsSelection(value: String, selectedPositions: IntArray, min: Int, max: Int) {
        if (selectedPositions.size < min) {
            Toast.makeText(context, getString(R.string.min_recipient_selected_error, min), Toast.LENGTH_LONG).show()
            return
        }
        if (selectedPositions.size > max) {
            Toast.makeText(context, getString(R.string.max_recipient_selected_error, max), Toast.LENGTH_LONG).show()
            return
        }
        recipientsSelection = selectedPositions
        recipientsText.text = String.format("%s%s", value, String.format(if (selectedPositions.size == 1) "" else " +%d", selectedPositions.size - 1))
        listDialogFragment?.dismiss()
    }

    private fun submitResolution() {
        inputMethodManager.hideSoftInputFromWindow(commentField.windowToken, 0)
        if (recipientsText.visibility == View.VISIBLE && recipientsText.text.isEmpty()) {
            recipientsText.requestFocus()
            recipientsText.error = getString(R.string.recipients_selection_required_text)
            return
        }
        respondObservable
                ?.concatMap { (first, second) ->
                    Observable.from(second)
                            .filter { (approving && it.value.contains("APP")) || (!approving && !it.value.contains("APP")) }
                            .map { Pair(first, it.value) }
                }
                ?.first()
                ?.map {
                    val recipientsIds = ArrayList<Int>()
                    (recipientsSelection ?: kotlin.IntArray(0)).asList().mapTo(recipientsIds) { recipients!![it].value.toInt() }
                    Triple(it.first, it.second, recipientsIds)
                }
                ?.map { if (it.third.isNotEmpty()) it else Triple(it.first, it.second, null) }
                ?.subscribe {
                    showProgressBar(true)
                    actionsCreator.sendRequestResolution(it.first, it.second, commentField.text.toString(), it.third)
                }
        val action = if (approving) "Approve" else "Decline"
        val commented = commentField.text.isNotEmpty().toString()
        tracker.trackEvent(AnalyticsCategories.TIME_OFF_APPROVAL, "Type", action)
        tracker.trackEvent(AnalyticsCategories.TIME_OFF_APPROVAL, "Comment", commented)
        tracker.trackEvent(AnalyticsCategories.TIME_OFF_APPROVAL, "Sequence", recipientSequence)
        tracker.trackFirebaseEvent(FirebaseEvents.TIME_OFF_APPROVAL_SUBMIT, "action", action, "commented", commented, "sequence", recipientSequence)
    }
}
