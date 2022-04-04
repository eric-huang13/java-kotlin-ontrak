package com.delphiaconsulting.timestar.action.creators

import com.delphiaconsulting.timestar.action.Actions
import com.delphiaconsulting.timestar.action.Keys
import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.net.gson.*
import com.delphiaconsulting.timestar.net.service.TimeOffService
import com.delphiaconsulting.timestar.util.Preferences
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
