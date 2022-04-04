package com.delphiaconsulting.timestar.view.service

import android.content.Context
import com.firebase.jobdispatcher.*
import com.delphiaconsulting.timestar.App
import com.delphiaconsulting.timestar.action.creators.PunchActionsCreator
import com.delphiaconsulting.timestar.event.OnOfflinePunchesSyncFailed
import com.delphiaconsulting.timestar.event.OnOfflinePunchesSynced
import com.delphiaconsulting.timestar.event.OnOfflinePunchesToSubmit
import com.delphiaconsulting.timestar.net.analytics.FirebaseEvents
import com.delphiaconsulting.timestar.net.analytics.Tracker
import com.delphiaconsulting.timestar.store.PunchStore
import com.delphiaconsulting.timestar.util.ConnectionUtil
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