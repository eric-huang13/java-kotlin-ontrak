package com.delphiaconsulting.timestar.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delphiaconsulting.timestar.R

abstract class MainTimeOffApprovalResolutionActivity : BaseActivity() {

    companion object {
        val APPROVING_FLAG_EXTRA = "APPROVING_FLAG_EXTRA"

        fun getCallingIntent(context: Context, approving: Boolean): Intent = Intent(context, TimeOffApprovalResolutionActivity::class.java).putExtra(APPROVING_FLAG_EXTRA, approving)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_off_approval_resolution)
    }

    override val titleResource: Int
        get() {
            if (intent.getBooleanExtra(APPROVING_FLAG_EXTRA, true)) {
                return R.string.activity_time_off_approve_title
            }
            return R.string.activity_time_off_decline_title
        }

    override val trackerScreen: String
        get() = "Time Off Approval Submission"
}
