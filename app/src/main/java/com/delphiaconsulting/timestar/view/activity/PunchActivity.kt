package com.delphiaconsulting.timestar.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.event.OnTrackPunchEvent
import com.delphiaconsulting.timestar.net.analytics.AnalyticsCategories
import org.greenrobot.eventbus.Subscribe

class PunchActivity : BaseActivity() {

    companion object {
        fun getCallingIntent(context: Context?): Intent = Intent(context, PunchActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_punch)
    }

    override fun registerInBus() = bus.register(this)

    override fun unregisterFromBus() = bus.unregister(this)

    override val titleResource: Int
        get() = R.string.activity_punch_title

    override val trackerScreen: String
        get() = "Punches"

    override val selfDrawerItem: Int
        get() = DRAWER_ITEM_PUNCHES

    @Subscribe
    fun onEvent(event: OnTrackPunchEvent) {
        tracker.trackEvent(if (event.offline) AnalyticsCategories.PUNCH_OFFLINE else AnalyticsCategories.PUNCH, event.actionText, event.labelText)
    }
}
