package com.insperity.escmobile.action.creators

import com.insperity.escmobile.BuildConfig
import com.insperity.escmobile.action.Actions
import com.insperity.escmobile.action.Keys
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.net.service.LegalService

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegalActionsCreator @Inject constructor(dispatcher: Dispatcher, private val legalService: LegalService) : ActionsCreator(dispatcher, null) {

    fun getLegal() = executeTask(legalService.getLegal(BuildConfig.LEGAL_ENDPOINT),
            { dispatcher.dispatch(Actions.LEGAL_RECEIVED, Keys.LEGAL_RESPONSE, it) },
            { dispatcher.dispatch(Actions.LEGAL_ERROR, Keys.ERROR, it) })
}
