package com.insperity.escmobile.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle

import com.insperity.escmobile.R

class AboutWebActivity : WebActivity() {

    companion object {
        val TITLE_EXTRA = "TITLE_EXTRA"
        val HTML_PAGE_EXTRA = "HTML_PAGE_EXTRA"

        fun getCallingIntent(context: Context, title: String, htmlPage: String): Intent = Intent(context, AboutWebActivity::class.java)
                .putExtra(TITLE_EXTRA, title)
                .putExtra(HTML_PAGE_EXTRA, htmlPage)
    }

    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_web)
        title = intent.getStringExtra(TITLE_EXTRA)
        supportActionBar?.title = title
    }

    override val titleResource: Int
        get() = 0

    override val trackerScreen: String
        get() = title ?: getString(R.string.flavor_app_name)
}
