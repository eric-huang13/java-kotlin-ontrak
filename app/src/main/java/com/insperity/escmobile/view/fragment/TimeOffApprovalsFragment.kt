package com.insperity.escmobile.view.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.FragmentStatePagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics.Event.VIEW_ITEM
import com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_CATEGORY
import com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME
import com.insperity.escmobile.R
import com.insperity.escmobile.action.creators.TimeOffActionsCreator
import com.insperity.escmobile.event.OnTimeOffApprovalRequestsReceived
import com.insperity.escmobile.event.OnTimeOffRequestClicked
import com.insperity.escmobile.event.OnTimeOffRequestDetailsError
import com.insperity.escmobile.event.OnTimeOffRequestDetailsReceived
import com.insperity.escmobile.net.analytics.AnalyticsCategories
import com.insperity.escmobile.net.analytics.Tracker
import com.insperity.escmobile.store.TimeOffStore
import com.insperity.escmobile.util.TimeOffStatuses
import com.insperity.escmobile.view.activity.MainTimeOffApprovalDetailsActivity
import com.insperity.escmobile.view.extension.snack
import kotlinx.android.synthetic.main.fragment_time_off_approvals.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class TimeOffApprovalsFragment : BaseFragment() {

    companion object {
        val TAG: String = TimeOffApprovalsFragment::class.java.simpleName
        private val PAGES_COUNT = 3
        private val ACTIVITY_REQUEST_CODE = 103
        val TIME_OFF_REQUEST_ID_EXTRA = "TIME_OFF_REQUEST_ID_EXTRA"
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var tracker: Tracker
    @Inject lateinit var actionsCreator: TimeOffActionsCreator
    @Inject lateinit var store: TimeOffStore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_time_off_approvals, container, false)

    private var requestEventId = ""

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            if (savedInstanceState == null && it.intent.hasExtra(TimeOffApprovalsFragment.TIME_OFF_REQUEST_ID_EXTRA)) {
                requestEventId = it.intent.getStringExtra(TimeOffApprovalsFragment.TIME_OFF_REQUEST_ID_EXTRA)
            }
        }
        component.inject(this)
        setupViewPager()
        if (savedInstanceState != null) return
        showProgressBar(true)
        actionsCreator.getRequestsToReview()
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        bus.removeStickyEvent(OnTimeOffApprovalRequestsReceived::class.java)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            showProgressBar(true)
            actionsCreator.getRequestsToReview()
        }
    }

    private fun setupViewPager() {
        viewPager.adapter = ViewPagerAdapter()
        viewPager.offscreenPageLimit = PAGES_COUNT
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                val currentTab = when (tab.position) {
                    1 -> "Approved"
                    2 -> "Declined"
                    else -> "Request"
                }
                tracker.trackEvent(AnalyticsCategories.TIME_OFF_APPROVAL, "Tab", currentTab)
                tracker.trackFirebaseEvent(VIEW_ITEM, ITEM_NAME, currentTab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    @Subscribe
    fun onEvent(event: OnTimeOffApprovalRequestsReceived) {
        if (requestEventId.isEmpty()) {
            showProgressBar(false)
            return
        }
        actionsCreator.getTimeOffRequestDetails(requestEventId)
    }

    @Subscribe
    fun onEvent(event: OnTimeOffRequestClicked) {
        showProgressBar(true)
        actionsCreator.getTimeOffRequestDetails(event.requestId)
        val tab = when (event.status) {
            TimeOffStatuses.APPROVED -> "Approved"
            TimeOffStatuses.DECLINED -> "Declined"
            else -> "Request"
        }
        tracker.trackEvent(AnalyticsCategories.TIME_OFF_APPROVAL, "Load Details", tab)
        tracker.trackFirebaseEvent(VIEW_ITEM, ITEM_CATEGORY, "Time Off Approval Details", ITEM_NAME, tab)
    }

    @Subscribe
    fun onEvent(event: OnTimeOffRequestDetailsReceived) {
        showProgressBar(false)
        requestEventId = ""
        activity?.let { startActivityForResult(MainTimeOffApprovalDetailsActivity.getCallingIntent(it), ACTIVITY_REQUEST_CODE) }
    }

    @Subscribe
    fun onEvent(event: OnTimeOffRequestDetailsError) {
        showProgressBar(false)
        requestEventId = ""
        viewPager.snack(event.message ?: getString(R.string.time_off_request_details_error_text)) {}
    }

    private inner class ViewPagerAdapter : FragmentStatePagerAdapter(childFragmentManager) {
        private val titles: Array<String> = resources.getStringArray(R.array.time_off_approval_tabs)

        override fun getItem(fragmentType: Int) = TimeOffApprovalTabFragment.newInstance(fragmentType)

        override fun getPageTitle(fragmentType: Int) = titles[fragmentType]

        override fun getCount() = PAGES_COUNT
    }
}
