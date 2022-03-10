package com.insperity.escmobile.net.analytics.impl

import android.app.Activity
import android.content.Context
import android.os.Bundle

import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.firebase.analytics.FirebaseAnalytics
import com.insperity.escmobile.BuildConfig
import com.insperity.escmobile.R
import com.insperity.escmobile.net.analytics.Tracker

import javax.inject.Singleton

/**
 * Created by dxsier on 2/23/17.
 */

@Singleton
class TrackerImpl(private val context: Context) : Tracker {
    private val googleTracker = GoogleAnalytics.getInstance(context).newTracker(R.xml.app_tracker)
    private val firebaseTracker = FirebaseAnalytics.getInstance(context)
    private var dryRun = false

    override fun setDryRun(dryRun: Boolean) {
        this.dryRun = dryRun
        GoogleAnalytics.getInstance(context).setDryRun(this.dryRun)
    }

    override fun setUserId(userId: String) {
        googleTracker.set("&uid", userId)
    }

    override fun trackDimension(index: Int, value: String) {
        send(HitBuilders.ScreenViewBuilder().setCustomDimension(index, value).build())
    }

    override fun trackScreenView(activity: Activity, path: String) {
        firebaseTracker.setCurrentScreen(activity, path, null)
        trackScreenView(path)
    }

    override fun trackScreenView(path: String) {
        send(HitBuilders.ScreenViewBuilder().build(), path)
    }

    override fun trackEvent(category: String, action: String) {
        send(HitBuilders.EventBuilder().setCategory(category).setAction(action).build())
    }

    override fun trackEvent(category: String, action: String, label: String) {
        send(HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build())
    }

    override fun trackEvent(category: String, action: String, label: String, value: Long) {
        send(HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).setValue(value).build())
    }

    private fun send(event: Map<String, String>, path: String? = null) {
        if (path != null) {
            googleTracker.setScreenName(path)
        }
        googleTracker.send(event)
        if (BuildConfig.DEBUG) {
            GoogleAnalytics.getInstance(context).dispatchLocalHits()
        }
    }

    override fun trackFirebaseEvent(eventType: String, vararg params: String) {
        if (params.size % 2 != 0) {
            throw IllegalArgumentException("Data must be a valid list of key, value pairs")
        }

        val bundle = Bundle()
        var i = 0
        while (i < params.size) {
            bundle.putString(params[i++], params[i++])
        }
        send(eventType, bundle)
    }

    private fun send(eventName: String, params: Bundle) {
        if (dryRun) return
        firebaseTracker.logEvent(eventName, params)
    }
}
