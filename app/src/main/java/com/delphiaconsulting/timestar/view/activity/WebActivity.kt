package com.delphiaconsulting.timestar.view.activity

import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.view.fragment.WebFragment

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
