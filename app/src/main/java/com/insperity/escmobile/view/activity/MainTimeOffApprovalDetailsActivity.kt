package com.insperity.escmobile.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.analytics.FirebaseAnalytics.Event.VIEW_ITEM
import com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME
import com.insperity.escmobile.R
import com.insperity.escmobile.net.analytics.AnalyticsCategories
import com.insperity.escmobile.view.fragment.TimeOffCalendarFragment
import com.insperity.escmobile.view.fragment.TimeOffRequestDetailsFragment
import kotlinx.android.synthetic.main.activity_time_off_approval_details.*

abstract class MainTimeOffApprovalDetailsActivity : BaseActivity() {

    companion object {
        private val DETAILS_FRAGMENT = 0
        private val CALENDAR_FRAGMENT = 1
        private val PAGES_COUNT = 2

        fun getCallingIntent(context: Context) = Intent(context, TimeOffApprovalDetailsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_off_approval_details)

        viewPager.offscreenPageLimit = getPageCount()
        viewPager.adapter = ViewPagerAdapter()
        viewPagerIndicator.setViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                val page = if (position == 0) "Details" else "Calendar"
                tracker.trackEvent(AnalyticsCategories.TIME_OFF_APPROVAL, "Page", page)
                tracker.trackFirebaseEvent(VIEW_ITEM, ITEM_NAME, page)
            }
        })
    }

    override val titleResource: Int
        get() = R.string.activity_time_off_approval_details_title

    override val trackerScreen: String
        get() = "Time Off Approval Details"

    private fun getPageCount() = PAGES_COUNT

    protected inner class ViewPagerAdapter : FragmentStatePagerAdapter(supportFragmentManager) {
        private val titles: Array<String> = resources.getStringArray(R.array.time_off_approval_tabs)

        override fun getItem(fragmentType: Int): Fragment {
            if (fragmentType == CALENDAR_FRAGMENT) {
                return TimeOffCalendarFragment.newInstance()
            }
            return TimeOffRequestDetailsFragment.newInstance()
        }

        override fun getPageTitle(fragmentType: Int) = titles[fragmentType]

        override fun getCount() = PAGES_COUNT
    }
}
