package com.insperity.escmobile.net.analytics

import android.app.Activity

/**
 * Created by dxsier on 2/23/17.
 */

interface Tracker {

    fun setDryRun(dryRun: Boolean)

    fun setUserId(userId: String)

    fun trackDimension(index: Int, value: String)

    fun trackScreenView(activity: Activity, path: String)

    fun trackScreenView(path: String)

    fun trackEvent(category: String, action: String)

    fun trackEvent(category: String, action: String, label: String)

    fun trackEvent(category: String, action: String, label: String, value: Long)

    fun trackFirebaseEvent(eventType: String, vararg params: String)
}
