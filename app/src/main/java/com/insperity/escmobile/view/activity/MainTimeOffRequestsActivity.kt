package com.insperity.escmobile.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnTimeOffSummaryReceived
import com.insperity.escmobile.view.fragment.TimeOffBalanceFragment
import com.insperity.escmobile.view.fragment.TimeOffRequestsFragment
import org.greenrobot.eventbus.Subscribe

abstract class MainTimeOffRequestsActivity : BaseActivity() {

    companion object {
        fun getCallingIntent(context: Context) = Intent(context, TimeOffRequestsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_off_requests)

        if (preferences.timeOffBalancesEnabled) {
            addFragment(R.id.timeOffBalanceFragmentContainer, TimeOffBalanceFragment.newInstance(), TimeOffBalanceFragment.TAG)
            return
        }
        addFragment(R.id.timeOffRequestsFragmentContainer, TimeOffRequestsFragment.newInstance(), TimeOffRequestsFragment.TAG)
    }

    override val titleResource: Int
        get() = R.string.activity_time_off_requests_title

    override val trackerScreen: String
        get() = "Time Off History"

    override fun registerInBus() = bus.register(this)

    override fun unregisterFromBus() = bus.unregister(this)

    @Subscribe
    fun onEvent(event: OnTimeOffSummaryReceived) = addFragment(R.id.timeOffRequestsFragmentContainer, TimeOffRequestsFragment.newInstance(), TimeOffRequestsFragment.TAG)

    private fun addFragment(containerId: Int, fragment: Fragment, tag: String) = supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(containerId, fragment, tag)
            .commit()
}
