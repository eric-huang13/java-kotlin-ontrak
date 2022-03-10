package com.insperity.escmobile.view.activity

import com.insperity.escmobile.R
import com.insperity.escmobile.view.fragment.WebFragment

/**
 * Created by dxsier on 3/31/17.
 */

abstract class WebActivity : BaseActivity() {

    private var fragment: WebFragment? = null

    override fun onBackPressed() {
        if (fragment == null) {
            fragment = supportFragmentManager.findFragmentById(R.id.web_fragment) as WebFragment
        }
        if (fragment!!.didWebViewGoBack()) {
            return
        }
        super.onBackPressed()
    }
}
