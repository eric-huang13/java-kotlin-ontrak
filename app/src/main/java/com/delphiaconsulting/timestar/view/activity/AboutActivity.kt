package com.delphiaconsulting.timestar.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle

import com.delphiaconsulting.timestar.R

class AboutActivity : BaseActivity() {

    companion object {
        fun getCallingIntent(context: Context) = Intent(context, AboutActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }

    override val titleResource: Int
        get() = R.string.title_activity_about

    override val trackerScreen: String
        get() = "About"

    override val selfDrawerItem: Int
        get() = DRAWER_ITEM_ABOUT
}