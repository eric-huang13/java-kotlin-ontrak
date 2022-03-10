package com.insperity.escmobile.view.service

import android.content.Context
import com.firebase.jobdispatcher.*
import com.insperity.escmobile.App
import com.insperity.escmobile.action.creators.PunchActionsCreator
import com.insperity.escmobile.event.OnOfflinePunchesSyncFailed
import com.insperity.escmobile.event.OnOfflinePunchesSynced
import com.insperity.escmobile.event.OnOfflinePunchesToSubmit
import com.insperity.escmobile.net.analytics.FirebaseEvents
import com.insperity.escmobile.net.analytics.Tracker
import com.insperity.escmobile.store.PunchStore
import com.insperity.escmobile.util.ConnectionUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class OfflinePunchJobService : JobService() {

    companion object {
        val TAG: String = OfflinePunchJobService::class.java.simpleName

        fun schedule(context: Context) {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
            dispatcher.mustSchedule(dispatcher.newJobBuilder()
                    .setService(OfflinePunchJobService::class.java)
                    .setTag(OfflinePunchJobService.TAG)
                    .setLifetime(Lifetime.FOREVER)
                    .setTrigger(Trigger.executionWindow(0, 60))
                    .setReplaceCurrent(false)
                    .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    .build())
        }
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var tracker: Tracker
    @Inject lateinit var connectionUtil: ConnectionUtil
    @Inject lateinit var actionsCreator: PunchActionsCreator
    @Inject lateinit var store: PunchStore

    private lateinit var job: JobParameters

    override fun onCreate() {
        super.onCreate()
        (application as App).component.inject(this)
        bus.register(this)
    }

    override fun onStartJob(job: JobParameters): Boolean {
        if (!connectionUtil.isConnected) {
            jobFinished(job, true)
            return false
        }
        this.job = job
        store.getOfflinePunchesToSync()
        return true
    }

    override fun onStopJob(job: JobParameters) = !connectionUtil.isConnected

    override fun onDestroy() {
        bus.unregister(this)
        super.onDestroy()
    }

    @Subscribe
    fun onEvent(event: OnOfflinePunchesToSubmit) {
        if (event.punchesToSubmit.punches.isEmpty()) {
            jobFinished(job, false)
            return
        }
        if (!connectionUtil.isConnected) {
            jobFinished(job, true)
            return
        }
        actionsCreator.submitOfflinePunches(event.punchesToSubmit)
    }

    @Subscribe
    fun onEvent(event: OnOfflinePunchesSynced) {
        jobFinished(job, false)
        tracker.trackFirebaseEvent(FirebaseEvents.PUNCH_SYNC, "status", "Success")
    }

    @Subscribe
    fun onEvent(event: OnOfflinePunchesSyncFailed) {
        jobFinished(job, true)
        tracker.trackFirebaseEvent(FirebaseEvents.PUNCH_SYNC, "status", "Failed")
    }
}