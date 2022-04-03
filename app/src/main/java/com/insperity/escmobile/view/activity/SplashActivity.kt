package com.insperity.escmobile.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.insperity.escmobile.App
import com.insperity.escmobile.R
import com.insperity.escmobile.action.creators.SessionActionsCreator
import com.insperity.escmobile.event.*
import com.insperity.escmobile.store.SessionStore
import com.insperity.escmobile.util.ConnectionUtil
import com.insperity.escmobile.util.Preferences
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.Subscriptions
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    companion object {
        private val STANDARD_SPLASH_TIMEOUT: Long = 1000
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var connectionUtil: ConnectionUtil
    @Inject lateinit var actionsCreator: SessionActionsCreator
    @Inject lateinit var store: SessionStore

    private var subscription = Subscriptions.empty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as App).component.inject(this)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)

        subscription = Observable.timer(STANDARD_SPLASH_TIMEOUT, TimeUnit.MILLISECONDS).replay().autoConnect()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (preferences.timeStarToken.isEmpty()) {
                        startNextActivity(OnboardingActivity.getCallingIntent(this))
                        return@subscribe
                    }
                    if (connectionUtil.isConnected) {
                        actionsCreator.getSessionData()
                        return@subscribe
                    }
                    startNextActivity(DashboardActivity.getCallingIntent(this))
                }
    }

    override fun onPause() {
        subscription.unsubscribe()
        bus.unregister(this)
        super.onPause()
    }

    @Subscribe
    fun onEvent(event: OnPunchesDataSaved) = startNextActivity(DashboardActivity.getCallingIntent(this))

    @Subscribe
    fun onEvent(event: OnPunchDataError) = startNextActivity(OnboardingActivity.getCallingIntent(this))

    @Subscribe
    fun onEvent(event: OnNoPunchDataError) = startNextActivity(OnboardingActivity.getCallingIntent(this))

    @Subscribe
    fun onEvent(event: OnSessionDataReceived) {
        if (!event.noPunchMode) return
        startNextActivity(DashboardActivity.getCallingIntent(this))
    }

    @Subscribe
    fun onEvent(event: OnTokenOrSessionDataError) = startNextActivity(DashboardActivity.getCallingIntent(this))

    @Subscribe
    fun onEvent(event: OnUnauthorizedAccess) {
        preferences.timeStarToken = ""
        startNextActivity(OnboardingActivity.getCallingIntent(this))
    }

    private fun startNextActivity(intent: Intent) {
        startActivity(intent)
        finish()
    }
}
