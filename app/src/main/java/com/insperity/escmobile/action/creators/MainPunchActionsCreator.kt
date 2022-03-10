package com.insperity.escmobile.action.creators

import android.util.Pair
import com.insperity.escmobile.action.Actions
import com.insperity.escmobile.action.Keys
import com.insperity.escmobile.data.PunchCategoryEntity
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.net.gson.*
import com.insperity.escmobile.net.service.TimePunchService
import com.insperity.escmobile.util.ConnectionUtil
import com.insperity.escmobile.util.Preferences
import rx.Observable
import java.util.concurrent.TimeUnit

/**
 * Created by dxsier on 12/27/16.
 */

abstract class MainPunchActionsCreator(dispatcher: Dispatcher, protected var timePunchService: TimePunchService, protected var preferences: Preferences, private val connectionUtil: ConnectionUtil) : MainActionsCreator(dispatcher) {

    fun syncPunchData(token: String = preferences.timeStarToken) = executeTask(Observable.zip(timePunchService.getServerTime(token), timePunchService.getPunchCategories(token), timePunchService.getOrgLevels(token), timePunchService.getOrgHierarchy(token), getOrgMainDefault(token),
            timePunchService.getOrgDefaults(token)) { serverTime, categories, orgLevels, orgHierarchy, orgMainDefaults, orgDefaults -> arrayListOf(serverTime, categories, orgLevels, orgHierarchy, orgMainDefaults, orgDefaults) }.doOnCompleted { getPunches(token) },
            { dispatcher.dispatch(Actions.PUNCH_SYNC_DATA_RECEIVED, Keys.PUNCH_CATEGORIES, Pair((it as ArrayList<*>)[0], it[1]), Keys.ORG_LEVELS_ITEMS, Pair(it[2], it[3]), Keys.DEFAULTS_DATA, Pair(it[4], it[5])) },
            { dispatcher.dispatch(Actions.PUNCH_SYNC_ERROR, Keys.ERROR, it) })

    private fun getPunches(token: String) = executeTask(getPunchesObservable(token),
            { dispatcher.dispatch(Actions.PUNCHES_DATA_RECEIVED, Keys.PUNCHES_DATA, it) },
            { dispatcher.dispatch(Actions.PUNCHES_DATA_ERROR, Keys.ERROR, it) })

    protected abstract fun getOrgMainDefault(token: String): Observable<Response<OrgDefaultList>>

    protected abstract fun getPunchesObservable(token: String): Observable<Response<PunchList>>

    fun submitPunch(punchCategory: PunchCategoryEntity, timestamp: Long, comment: String, selectedOrgLevels: List<Long>) = executeTask(Observable.from(selectedOrgLevels)
            .buffer(2)
            .map { PunchToSubmit.OrgLevelSelection(it[0], it[1]) }
            .toList()
            .map { PunchToSubmit(punchCategory.id, comment, it, timestamp) }
            .flatMap {
                if (!connectionUtil.isConnected) {
                    dispatcher.dispatch(Actions.SCHEDULE_OFFLINE_PUNCH_SUBMISSION, Keys.OFFLINE_PUNCH_SUBMISSION, it)
                    return@flatMap Observable.empty<Any>()
                }
                timePunchService.submitPunch(preferences.timeStarToken, it)
                        .doOnError { e ->
                            if (e is IllegalStateException) return@doOnError  //IllegalStateException means demo mode is active
                            dispatcher.dispatch(Actions.SCHEDULE_OFFLINE_PUNCH_SUBMISSION, Keys.OFFLINE_PUNCH_SUBMISSION, it)
                        }
                        .doOnCompleted { getPunches(preferences.timeStarToken) }
            },
            { dispatcher.dispatch(Actions.ONLINE_PUNCH_SUBMITTED, Keys.SUBMITTED_PUNCH, it) },
            { dispatcher.dispatch(Actions.ONLINE_PUNCH_SUBMISSION_ERROR, Keys.ERROR, it) })

    fun submitOfflinePunches(punchesToSubmit: PunchesToSubmit) = executeTask(timePunchService.submitOfflinePunches(preferences.timeStarToken, punchesToSubmit)
            .doOnNext {
                if (it.data?.success == 1) {
                    Observable.timer(1, TimeUnit.SECONDS).subscribe { getPunches(preferences.timeStarToken) }
                }
            },
            { dispatcher.dispatch(Actions.OFFLINE_PUNCHES_SUBMITTED, Keys.SUBMITTED_PUNCH, Pair(it, punchesToSubmit.punches.map { it.id })) },
            { dispatcher.dispatch(Actions.OFFLINE_PUNCHES_SUBMISSION_ERROR, Keys.ERROR, it) })
}
