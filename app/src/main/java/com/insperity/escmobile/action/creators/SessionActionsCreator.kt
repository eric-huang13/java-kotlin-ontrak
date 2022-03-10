package com.insperity.escmobile.action.creators

import android.content.Context
import android.content.Intent
import com.insperity.escmobile.action.Actions
import com.insperity.escmobile.action.Keys
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.net.gson.RegistrationRequest
import com.insperity.escmobile.net.service.SessionService
import com.insperity.escmobile.util.Preferences
import com.insperity.escmobile.util.TokenUtil
import com.insperity.escmobile.view.service.PunchDataService
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by dxsier on 1/19/17.
 */
@Singleton
class SessionActionsCreator @Inject constructor(dispatcher: Dispatcher, private val context: Context, private val preferences: Preferences, private val sessionService: SessionService) : MainActionsCreator(dispatcher) {

    fun getSessionData() = executeTask(getSessionDataAndPunchBaseDataTriggerObservable(preferences.timeStarToken),
            { dispatcher.dispatch(Actions.SESSION_DATA_RECEIVED, Keys.SESSION_DATA, it) },
            { dispatcher.dispatch(Actions.TOKEN_OR_SESSION_DATA_ERROR, Keys.ERROR, it) })

    fun submitAuthCode(authCode: String) = executeTask(sessionService.registerDevice(RegistrationRequest(authCode))
            .concatMap {
                if (it.data == null) {
                    dispatcher.dispatch(Actions.AUTH_TOKEN_ERROR, Keys.ERROR, it.message)
                    return@concatMap Observable.empty<Any>()
                }
                dispatcher.dispatch(Actions.AUTH_TOKEN_RECEIVED, Keys.AUTH_TOKEN, it.data.token)
                getSessionDataAndPunchBaseDataTriggerObservable(TokenUtil.formatItaAuthToken(it.data.token))
            },
            { dispatcher.dispatch(Actions.SESSION_DATA_RECEIVED, Keys.SESSION_DATA, it) },
            { dispatcher.dispatch(Actions.TOKEN_OR_SESSION_DATA_ERROR, Keys.ERROR, it) })

    private fun getSessionDataAndPunchBaseDataTriggerObservable(token: String) = sessionService.sessionData(token).doOnCompleted { context.startService(Intent(context, PunchDataService::class.java)) }
}
