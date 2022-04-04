package com.delphiaconsulting.timestar.view.service

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.delphiaconsulting.timestar.App
import com.delphiaconsulting.timestar.action.creators.PunchActionsCreator
import com.delphiaconsulting.timestar.event.OnNoPunchDataError
import com.delphiaconsulting.timestar.event.OnPunchBaseDataServiceStopped
import com.delphiaconsulting.timestar.event.OnPunchDataError
import com.delphiaconsulting.timestar.event.OnPunchesDataSaved
import com.delphiaconsulting.timestar.store.PunchStore
import com.delphiaconsulting.timestar.util.ConnectionUtil
import com.delphiaconsulting.timestar.util.Preferences
import com.delphiaconsulting.timestar.util.PunchMode
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
