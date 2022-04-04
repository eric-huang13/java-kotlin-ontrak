package com.delphiaconsulting.timestar.action.creators

import com.delphiaconsulting.timestar.action.Actions
import com.delphiaconsulting.timestar.action.Keys
import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.net.gson.*
import com.delphiaconsulting.timestar.net.service.TimeOffService
import com.delphiaconsulting.timestar.util.Preferences
import rx.Observable

abstract class MainTimeOffActionsCreator(dispatcher: Dispatcher, taskCache: Any?, protected val timeOffService: TimeOffService, protected var preferences: Preferences) : ActionsCreator(dispatcher, taskCache) {

    fun getSummary() = executeTask(getSummaryObservable(),
            { dispatcher.dispatch(Actions.TIME_OFF_SUMMARY_RECEIVED, Keys.TIME_OFF_BALANCES, it) },
            { dispatcher.dispatch(Actions.TIME_OFF_SUMMARY_ERROR, Keys.ERROR, it) })

    fun getTimeOffRequests() = executeTask(timeOffService.getTimeOffRequests(preferences.timeStarToken),
            { dispatcher.dispatch(Actions.TIME_OFF_REQUESTS_RECEIVED, Keys.TIME_OFF_REQUESTS, it) },
            { dispatcher.dispatch(Actions.TIME_OFF_REQUESTS_ERROR, Keys.ERROR, it) })

    fun getTimeOffRequestDetails(requestId: String) = executeTask(timeOffService.getTimeOffRequest(preferences.timeStarToken, requestId),
            { dispatcher.dispatch(Actions.TIME_OFF_REQUEST_DETAILS_RECEIVED, Keys.TIME_OFF_REQUEST_DETAILS, it) },
            { dispatcher.dispatch(Actions.TIME_OFF_REQUEST_DETAILS_ERROR, Keys.ERROR, it) })

    fun getRequestCreationData() = executeTask(Observable.zip(getBalancesObservable(), getBalancesMetadataObservable(), timeOffService.getTimeOffRequestsMetadata(preferences.timeStarToken)) { first, second, third -> Triple(first, second, third) },
            { dispatcher.dispatch(Actions.TIME_OFF_CREATION_DATA_RECEIVED, Keys.TIME_OFF_BALANCES, it) },
            { dispatcher.dispatch(Actions.TIME_OFF_CREATION_DATA_ERROR, Keys.ERROR, it) })

    fun getBalances(vararg dates: String) = executeTask(getBalancesObservable(*dates),
            { dispatcher.dispatch(Actions.TIME_OFF_DATES_BALANCES_RECEIVED, Keys.TIME_OFF_BALANCES, it) },
            { dispatcher.dispatch(Actions.TIME_OFF_DATES_BALANCES_ERROR, Keys.ERROR, it) })

    fun submitRequest(comment: String, recipients: List<Int>, dates: List<TimeOffRequestDate>) = executeTask(timeOffService.submitTimeOffRequest(preferences.timeStarToken, TimeOffSubmitRequest(comment, recipients, dates)),
            { dispatcher.dispatch(Actions.TIME_OFF_REQUEST_SUBMITTED, Keys.TIME_OFF_SUBMISSION_RESULT, it) },
            { dispatcher.dispatch(Actions.TIME_OFF_REQUEST_SUBMISSION_ERROR, Keys.ERROR, it) })

    fun getRequestsToReview() = executeTask(timeOffService.getTimeOffRequestsReview(preferences.timeStarToken),
            { dispatcher.dispatch(Actions.TIME_OFF_REQUESTS_REVIEW_RECEIVED, Keys.TIME_OFF_REQUESTS, it) },
            { dispatcher.dispatch(Actions.TIME_OFF_REQUESTS_REVIEW_ERROR, Keys.ERROR, it) })

    fun sendRequestResolution(requestId: String, resolutionCode: String, comment: String, recipientList: List<Int>?) = executeTask(timeOffService.respondTimeOffRequest(preferences.timeStarToken, requestId, TimeOffRequestResolution(resolutionCode, comment, recipientList)),
            { dispatcher.dispatch(Actions.TIME_OFF_RESOLUTION_REQUEST_RECEIVED, Keys.TIME_OFF_RESOLUTION_RESULT, it) },
            { dispatcher.dispatch(Actions.TIME_OFF_RESOLUTION_REQUEST_ERROR, Keys.ERROR, it) })

    protected abstract fun getSummaryObservable(): Observable<Response<TimeOffSummary>>

    protected abstract fun getBalancesObservable(vararg dates: String): Observable<Response<TimeOffBalances>>

    protected abstract fun getBalancesMetadataObservable(): Observable<Response<TimeOffBalancesMeta>>
}
