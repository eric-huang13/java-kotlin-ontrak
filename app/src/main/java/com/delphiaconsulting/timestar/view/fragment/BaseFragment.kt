package com.delphiaconsulting.timestar.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment

import com.delphiaconsulting.timestar.App
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.dagger.components.AppComponent
import com.delphiaconsulting.timestar.view.activity.BaseActivity

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
