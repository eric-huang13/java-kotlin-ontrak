package com.delphiaconsulting.timestar.action.creators

import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.net.gson.OrgDefaultList
import com.delphiaconsulting.timestar.net.gson.OrgDefaultRequest
import com.delphiaconsulting.timestar.net.gson.PunchList
import com.delphiaconsulting.timestar.net.gson.Response
import com.delphiaconsulting.timestar.net.service.TimePunchService
import com.delphiaconsulting.timestar.util.ConnectionUtil
import com.delphiaconsulting.timestar.util.Preferences
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by dxsier on 1/23/17.
 */

@Singleton
class PunchActionsCreator @Inject constructor(dispatcher: Dispatcher, timePunchService: TimePunchService, preferences: Preferences, connectionUtil: ConnectionUtil) : MainPunchActionsCreator(dispatcher, timePunchService, preferences, connectionUtil) {

    override fun getOrgMainDefault(token: String): Observable<Response<OrgDefaultList>> = timePunchService.getOrgMainDefault(token, OrgDefaultRequest(preferences.sessionEmployeeId))

    override fun getPunchesObservable(token: String): Observable<Response<PunchList>> {
        val now = DateTime()
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        return timePunchService.getPunches(token, preferences.sessionEmployeeId, formatter.print(now.minusDays(1)), formatter.print(now))
    }

    fun unsubscribeFromCurrentTask() {}
}
