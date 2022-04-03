package com.insperity.escmobile.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.TransitionManager
import androidx.core.content.ContextCompat
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import com.insperity.escmobile.R
import com.insperity.escmobile.action.creators.PunchActionsCreator
import com.insperity.escmobile.data.OrgDefaultEntity
import com.insperity.escmobile.data.OrgLevelEntity
import com.insperity.escmobile.data.PunchCategoryEntity
import com.insperity.escmobile.event.*
import com.insperity.escmobile.store.PunchStore
import com.insperity.escmobile.util.ConnectionUtil
import com.insperity.escmobile.util.Preferences
import com.insperity.escmobile.util.PunchMode
import com.insperity.escmobile.view.extension.onItemSelected
import com.insperity.escmobile.view.extension.snack
import com.insperity.escmobile.view.service.PunchDataService
import com.insperity.escmobile.view.widget.PunchOrgLevelView
import kotlinx.android.synthetic.main.fragment_punch_categories.*
import kotlinx.android.synthetic.main.widget_footnote.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class PunchCategoriesFragment : BaseFragment() {

    companion object {
        const val FAKE_HINT_PUNCH_CATEGORY_ID = 0L
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var inputMethodManager: InputMethodManager
    @Inject lateinit var connectionUtil: ConnectionUtil
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var actionsCreator: PunchActionsCreator
    @Inject lateinit var store: PunchStore

    private var serverTime: Long = 0
    private var punchOrgLevelView: PunchOrgLevelView? = null
    private var rootOrgDefault: OrgDefaultEntity? = null
    private var areDefaultOrgLevelsSelected: Boolean = false

    private var generalCategories: List<PunchCategoryEntity>? = null
    private var transferCategories: List<PunchCategoryEntity>? = null
    private var selectedPunchCategory: PunchCategoryEntity? = null
    private var transferTabSelected: Boolean = false
    private var wasTransferSelected: Boolean = false
    private var wasSearchUsed: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_punch_categories, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
        context?.let { if (!PunchDataService.isServiceRunning(it)) store.getPunchCategories() }
        defaultOrgLevelSwitch.setOnCheckedChangeListener { _, checked -> onDefaultOrgLevelSwitchChanged(checked) }
        punchButton.setOnClickListener { onSubmitButtonClicked() }
        bus.post(OnTrackPunchEvent(!connectionUtil.isConnected, "Change Org Allowed", preferences.allowOrgLevelDefaultsSwitch.toString()))
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
    fun onEvent(event: OnServerTimeTicked) {
        serverTime = event.time
    }

    @Subscribe
    fun onEvent(event: OnOrgLevelChanged) {
        areDefaultOrgLevelsSelected = false
    }

    @Subscribe
    fun onEvent(event: OnPunchOrgItemSelected) {
        if (!event.wasSearchUsed) return
        wasSearchUsed = true
    }

    @Subscribe
    fun onEvent(event: OnPunchBaseDataServiceStopped) {
        store.getPunchCategories()
    }

    @Subscribe
    fun onEvent(event: OnPunchCategoriesLoadError) {
        onNoPunchCategories()
    }

    @Subscribe
    fun onEvent(event: OnPunchCategoriesLoaded) {
        generalCategories = event.punchCategories?.first
        transferCategories = event.punchCategories?.second
        rootOrgDefault = event.rootOrgDefault
        punchOrgLevelView = getPunchOrgLevelView(event.rootOrgLevel)
        restoreOrgLevelDefaults()
        if (generalCategories?.isNotEmpty() == true) {
            if (categorySpinner.adapter != null) return
            generalCategories?.let { setupPunchCategoriesSpinner(it) }
            return
        }
        if (transferCategories?.isEmpty() == true) {
            onNoPunchCategories()
            return
        }
        if (transferCategories?.size == 1) {
            transferCategories?.let { onCategorySelected(it[0]) }
            return
        }
        if (categorySpinner.adapter != null) return
        transferCategories?.let { setupPunchCategoriesSpinner(it) }
    }

    private fun onNoPunchCategories() {
        punchContainer.visibility = View.GONE
        footnoteText.visibility = View.VISIBLE
        footnoteText.setText(R.string.punch_no_available_hint_text)
    }

    private fun setupPunchCategoriesSpinner(punchCategories: List<PunchCategoryEntity>, selectHintItem: Boolean = true) {
        val categoryAdapter = object : ArrayAdapter<PunchCategoryEntity>(requireContext(), android.R.layout.simple_spinner_item, punchCategories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = parent?.let { super.getView(position, convertView, it) } as TextView
                textView.text = getItem(position)?.description
                textView.setTextColor(ContextCompat.getColor(context, if (position == count) R.color.text_grey_light else R.color.text_black))
                return textView
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val textView = super.getDropDownView(position, convertView, parent) as TextView
                textView.text = getItem(position)?.description
                return textView
            }

            override fun getItem(position: Int): PunchCategoryEntity? {
                if (position == count) {
                    val punchCategory = PunchCategoryEntity()
                    punchCategory.id = FAKE_HINT_PUNCH_CATEGORY_ID
                    punchCategory.description = getString(R.string.select_punch_category_hint_text)
                    return punchCategory
                }
                return super.getItem(position)
            }
        }
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter
        categorySpinner.onItemSelected { _, position -> onCategorySelected(categorySpinner.getItemAtPosition(position) as PunchCategoryEntity) }
        if (selectHintItem) {
            categorySpinner.setSelection(categoryAdapter.count, false)
        }
    }

    private fun onCategorySelected(category: PunchCategoryEntity) {
        selectedPunchCategory = category
        if (selectedPunchCategory?.id == FAKE_HINT_PUNCH_CATEGORY_ID) {
            selectedPunchCategory = null
        }
        if (wasTransferSelected) {
            defaultOrgLevelSwitch.isChecked = true
        }
        wasTransferSelected = selectedPunchCategory?.defaultOrgLevel == false
        var visibilityArray = showCategorySpinnerConfig(!wasTransferSelected || (transferCategories?.size ?: 0) > 1, SparseIntArray())
        visibilityArray = showDefaultSwitchConfig(!wasTransferSelected && selectedPunchCategory != null && preferences.allowOrgLevelDefaultsSwitch, visibilityArray)
        visibilityArray = showOrgLevelsConfig(if (wasTransferSelected) true else !defaultOrgLevelSwitch.isChecked, visibilityArray)
        updateVisibilityConstrains(visibilityArray)
    }

    @SuppressLint("InflateParams")
    private fun getPunchOrgLevelView(orgLevel: OrgLevelEntity?): PunchOrgLevelView? = orgLevel?.let {
        val punchOrgLevelView = LayoutInflater.from(context).inflate(R.layout.view_punch_org_level, null) as PunchOrgLevelView
        punchOrgLevelView.setOrgLevel(orgLevel)
        orgLevelContainer.addView(punchOrgLevelView)
        punchOrgLevelView.setNextPunchOrgLevelView(getPunchOrgLevelView(orgLevel.next))
        punchOrgLevelView
    }

    private fun onDefaultOrgLevelSwitchChanged(enabled: Boolean) {
        bus.post(OnTrackPunchEvent(!connectionUtil.isConnected, "DefaultOrgLevel", if (enabled) "On" else "Off"))
        if (enabled) {
            restoreOrgLevelDefaults()
        }
        val visibilityArray = showOrgLevelsConfig(!enabled, SparseIntArray())
        updateVisibilityConstrains(visibilityArray)
    }

    private fun restoreOrgLevelDefaults() = punchOrgLevelView?.let {
        it.setupOrgItemsSpinner(null)
        it.setDefaultOrgItemSelection(rootOrgDefault)
        areDefaultOrgLevelsSelected = true
        wasSearchUsed = false
    }

    private fun onSubmitButtonClicked() {
        hideKeyboard()
        if (!connectionUtil.isConnected && preferences.punchMode != PunchMode.PUNCH_OFFLINE_MODE) {
            punchContainer.snack(R.string.not_connected_punch_submission_text) {}
            return
        }
        selectedPunchCategory?.let {
            val isOffline = !connectionUtil.isConnected
            actionsCreator.submitPunch(it, serverTime, commentField.text.toString().trim { it <= ' ' }, punchOrgLevelView?.getOrgLevelSelections() ?: ArrayList())
            bus.post(OnShowProgressBar(true))
            bus.post(OnTrackPunchEvent(isOffline, "New", "Submit"))
            bus.post(OnTrackPunchEvent(isOffline, "Has Change Org", areDefaultOrgLevelsSelected.toString()))
            bus.post(OnTrackPunchEvent(isOffline, "Used Org Search", wasSearchUsed.toString()))
        } ?: punchContainer.snack(R.string.select_category_error_text) {}
    }

    @Subscribe
    fun onEvent(event: OnPunchDataError) {
        bus.post(OnShowProgressBar(false))
    }

    @Subscribe
    fun onEvent(event: OnPunchSubmissionError) {
        bus.post(OnShowProgressBar(false))
        punchContainer.snack(event.message ?: getString(R.string.punch_submission_error_text)) {}
    }

    @Subscribe
    fun onEvent(event: OnPunchCategoryTabTapped) {
        transferTabSelected = event.isTransfer
        if (transferTabSelected) {
            transferCategories?.let {
                setupPunchCategoriesSpinner(it, it.size > 1)
                if (it.size == 1) {
                    onCategorySelected(it[0])
                }
                return@onEvent
            }
        }
        generalCategories?.let { setupPunchCategoriesSpinner(it) }
    }

    @Subscribe
    fun onEvent(event: OnPunchesDataSaved) {
        bus.post(OnShowProgressBar(false))
        cleanUp()
    }

    @Subscribe
    fun onEvent(event: OnOfflinePunchStored) {
        bus.post(OnShowProgressBar(false))
        punchContainer.snack(R.string.offline_mode_submitted_punch_text) {}
        cleanUp()
    }

    private fun cleanUp() {
        hideKeyboard()
        commentField.setText("")
        restoreOrgLevelDefaults()
    }

    private fun hideKeyboard() = inputMethodManager.hideSoftInputFromWindow(punchContainer.windowToken, 0)

    private fun showCategorySpinnerConfig(show: Boolean, visibilityArray: SparseIntArray): SparseIntArray {
        val visibility = if (show) View.VISIBLE else View.GONE
        if (categorySpinner.visibility != visibility) {
            visibilityArray.put(R.id.categorySpinner, visibility)
        }
        return visibilityArray
    }

    private fun showDefaultSwitchConfig(show: Boolean, visibilityArray: SparseIntArray): SparseIntArray {
        val visibility = if (show) View.VISIBLE else View.GONE
        if (defaultOrgLevelsText.visibility != visibility) {
            visibilityArray.put(R.id.defaultOrgLevelsText, visibility)
        }
        if (defaultOrgLevelSwitch.visibility != visibility) {
            visibilityArray.put(R.id.defaultOrgLevelSwitch, visibility)
        }
        return visibilityArray
    }

    private fun showOrgLevelsConfig(show: Boolean, visibilityArray: SparseIntArray): SparseIntArray {
        val visibility = if (show) View.VISIBLE else View.GONE
        if (orgLevelHeaderText.visibility != visibility) {
            visibilityArray.put(R.id.orgLevelHeaderText, visibility)
        }
        if (orgLevelContainer.visibility != visibility) {
            visibilityArray.put(R.id.orgLevelContainer, visibility)
        }
        return visibilityArray
    }

    private fun updateVisibilityConstrains(visibilityArray: SparseIntArray) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(punchContainer)
        var i = 0
        var disappearingAppearingViewsCount = 0
        while (i < visibilityArray.size()) {
            if (visibilityArray.valueAt(i) == View.GONE) {
                disappearingAppearingViewsCount--
            } else {
                disappearingAppearingViewsCount++
            }
            constraintSet.setVisibility(visibilityArray.keyAt(i), visibilityArray.valueAt(i++))
        }
        bus.post(OnNotifyPunchContainerTransition(disappearingAppearingViewsCount))
        TransitionManager.beginDelayedTransition(punchContainer)
        constraintSet.applyTo(punchContainer)
        hideKeyboard()
    }
}
