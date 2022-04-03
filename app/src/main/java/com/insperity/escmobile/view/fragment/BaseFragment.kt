package com.insperity.escmobile.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment

import com.insperity.escmobile.App
import com.insperity.escmobile.R
import com.insperity.escmobile.dagger.components.AppComponent
import com.insperity.escmobile.view.activity.BaseActivity

/**
 * Created by dxsier on 11/18/16.
 */

open class BaseFragment : Fragment() {

    protected val component: AppComponent
        get() = (activity?.applicationContext as App).component

    protected val isOrientationLandscape: Boolean
        get() = activity?.resources?.getBoolean(R.bool.is_landscape) ?: false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    protected open fun showProgressBar(show: Boolean) {
        if (activity == null || activity !is BaseActivity) return
        (activity as BaseActivity).showProgressBar(show)
    }
}
