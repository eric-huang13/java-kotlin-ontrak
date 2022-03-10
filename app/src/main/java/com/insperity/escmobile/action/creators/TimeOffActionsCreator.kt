package com.insperity.escmobile.action.creators

import com.insperity.escmobile.action.Actions
import com.insperity.escmobile.action.Keys
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.net.gson.*
import com.insperity.escmobile.net.service.TimeOffService
import com.insperity.escmobile.util.Preferences
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeOffActionsCreator @Inject constructor(dispatcher: Dispatcher, timeOffService: TimeOffService, preferences: Preferences) : MainTimeOffActionsCreator(dispatcher, null, timeOffService, preferences) {

    override fun getSummaryObservable(): Observable<Response<TimeOffSummary>> = timeOffService.getSummary(preferences.timeStarToken, preferences.sessionEmployeeId)

    override fun getBalancesObservable(vararg dates: String): Observable<Response<TimeOffBalances>> = timeOffService.getBalances(preferences.timeStarToken, preferences.sessionEmployeeId,
            *(if (dates.isNotEmpty()) dates else arrayOf(DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime.now()))))

    override fun getBalancesMetadataObservable(): Observable<Response<TimeOffBalancesMeta>> = timeOffService.getBalancesMetadata(preferences.timeStarToken, preferences.sessionEmployeeId)

    fun getLatestRequest(requestDetails: TimeOffRequestDetails) = executeTask(Observable.just(requestDetails),
            { dispatcher.dispatch(Actions.LATEST_TIME_OFF_REQUEST_RECEIVED, Keys.TIME_OFF_REQUEST_DETAILS, it) },
            { dispatcher.dispatch(Actions.LATEST_TIME_OFF_REQUEST_ERROR, Keys.ERROR, it) })

    fun getPendingRequestAmount() = executeTask(timeOffService.getTimeOffRequestsReview(preferences.timeStarToken),
            { dispatcher.dispatch(Actions.TIME_OFF_REQUESTS_FOR_PENDING_AMOUNT_RECEIVED, Keys.TIME_OFF_REQUESTS, it) },
            { dispatcher.dispatch(Actions.TIME_OFF_REQUESTS_FOR_PENDING_AMOUNT_ERROR, Keys.ERROR, it) })
}
