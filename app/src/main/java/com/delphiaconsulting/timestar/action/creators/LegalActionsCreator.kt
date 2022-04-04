package com.delphiaconsulting.timestar.action.creators

import com.delphiaconsulting.timestar.BuildConfig
import com.delphiaconsulting.timestar.action.Actions
import com.delphiaconsulting.timestar.action.Keys
import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.net.service.LegalService

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegalActionsCreator @Inject constructor(dispatcher: Dispatcher, private val legalService: LegalService) : ActionsCreator(dispatcher, null) {

    fun getLegal() = executeTask(legalService.getLegal(BuildConfig.LEGAL_ENDPOINT),
            { dispatcher.dispatch(Actions.LEGAL_RECEIVED, Keys.LEGAL_RESPONSE, it) },
            { dispatcher.dispatch(Actions.LEGAL_ERROR, Keys.ERROR, it) })
}
