package com.insperity.escmobile.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnTimeEntryItemClicked
import com.insperity.escmobile.net.analytics.AnalyticsCategories.TIMESHEET
import com.insperity.escmobile.net.analytics.AnalyticsCategories.TIMESHEET_SUP
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.Subscribe

abstract class MainTimeEntryDetailActivity : BaseActivity() {

    companion object {
        fun getCallingIntent(context: Context): Intent = Intent(context, TimeEntryDetailActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_entry_detail)
    }

    override fun setupToolbar() {
        super.setupToolbar()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
    }

    override fun setupToolbarListener() {
        toolbar?.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_down)
        }
    }

    override fun registerInBus() {
        bus.register(this)
    }

    override fun unregisterFromBus() {
        bus.unregister(this)
    }

    override val titleResource: Int
        get() = 0

    override val trackerScreen: String
        get() = "Timesheet Detail"

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeEntryItemClicked) {
        supportActionBar?.title = event.detailItem.title
        tracker.trackEvent(if (event.supervisorAccessed) TIMESHEET_SUP else TIMESHEET, "Load Details", event.detailItem.trackerModal)
    }
}
