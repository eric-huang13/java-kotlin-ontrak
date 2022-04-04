package com.delphiaconsulting.timestar.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.event.OnTimeOffSummaryReceived
import com.delphiaconsulting.timestar.view.fragment.TimeOffBalanceFragment
import com.delphiaconsulting.timestar.view.fragment.TimeOffRequestsFragment
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
