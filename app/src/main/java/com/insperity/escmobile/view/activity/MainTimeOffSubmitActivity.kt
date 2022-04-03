package com.insperity.escmobile.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.avast.android.dialogs.fragment.SimpleDialogFragment
import com.avast.android.dialogs.iface.ISimpleDialogListener
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnAddTimeOffDates
import com.insperity.escmobile.event.OnTimeOffBalancesReceived
import com.insperity.escmobile.event.OnTimeOffDatesSelected
import com.insperity.escmobile.net.analytics.AnalyticsCategories
import com.insperity.escmobile.view.fragment.TimeOffAddDateFragment
import com.insperity.escmobile.view.fragment.TimeOffSubmitFragment
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.Subscribe

abstract class MainTimeOffSubmitActivity : BaseActivity(), ISimpleDialogListener {

    companion object {
        private const val TIME_OFF_DROP_NEW_REQUEST_CONFIRM_DIALOG = 1

        fun getCallingIntent(context: Context) = Intent(context, TimeOffSubmitActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_off_submit)
        replaceFragment(TimeOffSubmitFragment.newInstance(), TimeOffSubmitFragment.TAG, R.string.activity_time_off_submit_title, false)
    }

    override fun onDestroy() {
        bus.removeStickyEvent(OnTimeOffBalancesReceived::class.java)
        super.onDestroy()
    }

    override fun registerInBus() = bus.register(this)

    override fun unregisterFromBus() = bus.unregister(this)

    override val titleResource: Int
        get() = R.string.activity_time_off_submit_title

    override val trackerScreen: String
        get() = "Time Off Request"

    override fun setupToolbarListener() {
        toolbar?.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag(TimeOffAddDateFragment.TAG) != null) {
            supportFragmentManager.popBackStack()
            setToolbarTitle(R.string.activity_time_off_submit_title)
            return
        }
        val fragment: TimeOffSubmitFragment? = supportFragmentManager.findFragmentByTag(TimeOffSubmitFragment.TAG)
        if (fragment == null) {
            super.onBackPressed()
            return
        }
        if (!fragment.pendingChanges()) {
            cancelRequest()
            return
        }
        SimpleDialogFragment.createBuilder(this, supportFragmentManager)
                .setMessage(R.string.time_off_discard_new_request_text)
                .setPositiveButtonText(R.string.ok_btn_text)
                .setNegativeButtonText(R.string.cancel_btn_text)
                .setRequestCode(TIME_OFF_DROP_NEW_REQUEST_CONFIRM_DIALOG)
                .show()
    }

    @Subscribe
    fun onEvent(event: OnAddTimeOffDates) = replaceFragment(TimeOffAddDateFragment.newInstance(), TimeOffAddDateFragment.TAG, R.string.activity_time_off_add_dates_title, true)

    @Subscribe
    fun onEvent(event: OnTimeOffDatesSelected) = onBackPressed()

    private fun replaceFragment(fragment: Fragment, tag: String, titleRes: Int, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragmentContainer, fragment, tag)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
        setToolbarTitle(titleRes)
        tracker.trackScreenView(this, if (tag == TimeOffAddDateFragment.TAG) "Time Off Calendar" else trackerScreen)
    }

    private fun setToolbarTitle(titleRes: Int) = supportActionBar?.setTitle(titleRes)

    override fun onNegativeButtonClicked(requestCode: Int) {}

    override fun onNeutralButtonClicked(requestCode: Int) {}

    override fun onPositiveButtonClicked(requestCode: Int) {
        if (requestCode != TIME_OFF_DROP_NEW_REQUEST_CONFIRM_DIALOG) return
        cancelRequest()
    }

    private fun cancelRequest() {
        finish()
        tracker.trackEvent(AnalyticsCategories.TIME_OFF, "New", "Canceled")
    }
}
