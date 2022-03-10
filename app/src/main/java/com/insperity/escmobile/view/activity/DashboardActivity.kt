package com.insperity.escmobile.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.insperity.escmobile.R
import com.insperity.escmobile.util.PunchMode
import com.insperity.escmobile.view.fragment.PunchWidgetFragment
import com.insperity.escmobile.view.fragment.TimeOffApprovalWidgetFragment
import com.insperity.escmobile.view.fragment.TimeOffRequestWidgetFragment
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : BaseActivity() {

    companion object {
        fun getCallingIntent(context: Context) = Intent(context, DashboardActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setupPunchWidget()
        setupTimeOffApprovalWidget()
        setupTimeOffRequestWidget()
    }

    override val titleResource: Int
        get() = R.string.flavor_app_name

    override val trackerScreen: String
        get() = "Dashboard"

    override val selfDrawerItem: Int
        get() = DRAWER_ITEM_DASHBOARD

    private fun setupPunchWidget() {
        if (preferences.punchMode == PunchMode.NO_PUNCH_MODE) {
            return
        }
        var fragment = supportFragmentManager.findFragmentByTag(PunchWidgetFragment.TAG)
        if (fragment == null) {
            fragment = PunchWidgetFragment.newInstance()
        }
        noContentText.visibility = View.GONE
        punchWidgetFragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction().add(R.id.punchWidgetFragmentContainer, fragment).commit()
    }

    private fun setupTimeOffApprovalWidget() {
        if (!preferences.timeOffApprovalEnabled) {
            return
        }
        var fragment = supportFragmentManager.findFragmentByTag(TimeOffApprovalWidgetFragment.TAG)
        if (fragment == null) {
            fragment = TimeOffApprovalWidgetFragment.newInstance()
        }
        noContentText.visibility = View.GONE
        timeOffApprovalWidgetFragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction().add(R.id.timeOffApprovalWidgetFragmentContainer, fragment).commit()
    }

    private fun setupTimeOffRequestWidget() {
        if (!preferences.timeOffBalancesEnabled) return
        var fragment = supportFragmentManager.findFragmentByTag(TimeOffRequestWidgetFragment.TAG)
        if (fragment == null) {
            fragment = TimeOffRequestWidgetFragment.newInstance()
        }
        noContentText.visibility = View.GONE
        timeOffRequestWidgetFragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction().add(R.id.timeOffRequestWidgetFragmentContainer, fragment).commit()
    }
}
