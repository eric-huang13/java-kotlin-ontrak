package com.delphiaconsulting.timestar.view.activity

import kotlinx.android.synthetic.main.toolbar.*

class TimeEntryActivity : MainTimeEntryActivity() {

    override fun setupToolbarListener() = toolbar.setNavigationOnClickListener { onBackPressed() }
}
