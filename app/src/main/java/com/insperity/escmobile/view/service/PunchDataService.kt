package com.insperity.escmobile.view.service

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.insperity.escmobile.App
import com.insperity.escmobile.action.creators.PunchActionsCreator
import com.insperity.escmobile.event.OnNoPunchDataError
import com.insperity.escmobile.event.OnPunchBaseDataServiceStopped
import com.insperity.escmobile.event.OnPunchDataError
import com.insperity.escmobile.event.OnPunchesDataSaved
import com.insperity.escmobile.store.PunchStore
import com.insperity.escmobile.util.ConnectionUtil
import com.insperity.escmobile.util.Preferences
import com.insperity.escmobile.util.PunchMode
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class PunchDataService : Service() {

    companion object {
        fun isServiceRunning(context: Context): Boolean {
            val runningServices = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getRunningServices(Integer.MAX_VALUE)
            return runningServices.any { it.service.className == PunchDataService::class.java.name }
        }
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var connectionUtil: ConnectionUtil
    @Inject lateinit var actionsCreator: PunchActionsCreator
    @Inject lateinit var store: PunchStore

    override fun onCreate() {
        super.onCreate()
        (application as App).component.inject(this)
        bus.register(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startCommand()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        actionsCreator.unsubscribeFromCurrentTask()
        bus.unregister(this)
        super.onDestroy()
        bus.post(OnPunchBaseDataServiceStopped())
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun startCommand() {
        if (preferences.timeStarToken.isEmpty() || !connectionUtil.isConnected || preferences.punchMode == PunchMode.NO_PUNCH_MODE) {
            stopSelf()
            return
        }
        actionsCreator.syncPunchData()
    }

    @Subscribe
    fun onEvent(event: OnPunchesDataSaved) = stopSelf()

    @Subscribe
    fun onEvent(event: OnPunchDataError) = stopSelf()

    @Subscribe
    fun onEvent(event: OnNoPunchDataError) = stopSelf()
}
