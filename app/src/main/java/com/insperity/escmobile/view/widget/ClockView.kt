package com.insperity.escmobile.view.widget

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.insperity.escmobile.App
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnServerTimeTicked
import com.insperity.escmobile.util.Preferences
import kotlinx.android.synthetic.main.view_punch_clock.view.*
import org.greenrobot.eventbus.EventBus
import org.joda.time.format.DateTimeFormat
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.Subscriptions
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ClockView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    companion object {
        private const val FORMAT_12_HOURS = "hh:mm"
        private const val FORMAT_24_HOURS = "k:mm"
        private const val FORMAT_AM_PM = "aa"
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var preferences: Preferences

    private var timeFormatter = DateTimeFormat.forPattern(FORMAT_12_HOURS)
    private var amPmFormatter = DateTimeFormat.forPattern(FORMAT_AM_PM)
    private var subscription = Subscriptions.empty()
    private var tickerStopped = false

    init {
        LayoutInflater.from(context).inflate(R.layout.view_punch_clock, this, true)
        (context.applicationContext as App).component.inject(this)
        initClock()
    }

    private fun initClock() {
        FormatChangeObserver()
        setFormat()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val serverTimeOffset = preferences.serverTime - preferences.serverTimeSetTime
        setTimer(serverTimeOffset)
    }

    private fun setTimer(serverTimeOffset: Long) {
        val nextInterval = 1000 - SystemClock.uptimeMillis() % 1000
        subscription.unsubscribe()
        subscription = Observable.timer(nextInterval, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setTimer(serverTimeOffset) }
        if (tickerStopped) return
        val timeInMillis = System.currentTimeMillis() + serverTimeOffset
        timeText.text = timeFormatter.print(timeInMillis)
        amPmText.text = amPmFormatter.print(timeInMillis).toString().toUpperCase(Locale.US)
        bus.post(OnServerTimeTicked(timeInMillis))
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        tickerStopped = visibility != View.VISIBLE
    }

    override fun onDetachedFromWindow() {
        subscription.unsubscribe()
        super.onDetachedFromWindow()
    }

    private fun setFormat() {
        val is24HourFormat = DateFormat.is24HourFormat(context)
        timeFormatter = DateTimeFormat.forPattern(if (is24HourFormat) FORMAT_24_HOURS else FORMAT_12_HOURS)
        amPmText.visibility = if (is24HourFormat) View.GONE else View.VISIBLE
    }

    fun setTimeTextColor(colorResId: Int) {
        val color = ContextCompat.getColor(context, colorResId)
        timeText.setTextColor(color)
        amPmText.setTextColor(color)
    }

    private inner class FormatChangeObserver : ContentObserver(Handler()) {
        init {
            context.contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, this)
        }

        override fun onChange(selfChange: Boolean) = setFormat()
    }
}
