package com.insperity.escmobile.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnSelectOrgLevel
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.Subscribe

abstract class MainOrgItemsSearchActivity : BaseActivity() {

    companion object {
        fun getCallingIntent(context: Context) = Intent(context, OrgItemsSearchActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_org_level_search)
    }

    override fun setupToolbar() {
        super.setupToolbar()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.stay, R.anim.slide_down)
    }

    override fun setupToolbarListener() {
        toolbar?.setNavigationOnClickListener { onBackPressed() }
    }

    override fun registerInBus() = bus.register(this)

    override fun unregisterFromBus() = bus.unregister(this)

    override val titleResource: Int
        get() = 0

    override val trackerScreen: String
        get() = "Time Punch Org Item Search"

    @Subscribe(sticky = true)
    fun onEvent(event: OnSelectOrgLevel) {
        supportActionBar?.title = event.orgLevel.name
    }
}
