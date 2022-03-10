package com.insperity.escmobile.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle

import com.insperity.escmobile.R

class TimeOffRequestDetailsActivity : BaseActivity() {

    companion object {
        fun getCallingIntent(context: Context) = Intent(context, TimeOffRequestDetailsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_off_request_details)
    }

    override val titleResource: Int
        get() = R.string.activity_time_off_request_details_title

    override val trackerScreen: String
        get() = "Time Off Request Detail"
}
