package com.insperity.escmobile.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView

import com.insperity.escmobile.view.activity.AboutWebActivity

class AboutWebFragment : WebFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState == null) {
            activity?.let {
                val htmlPage = it.intent.getStringExtra(AboutWebActivity.HTML_PAGE_EXTRA)
                loadData(htmlPage)
            }
        }
    }

    override fun onPageStartedListener(view: WebView, url: String) {}

    override fun shouldOverrideUrlLoadingListener(view: WebView, url: String): Boolean {
        if (url.startsWith("mailto:")) {
            val i = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
            startActivity(i)
            return true
        }
        return super.shouldOverrideUrlLoadingListener(view, url)
    }
}