package com.insperity.escmobile.action.creators

import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.net.gson.OrgDefaultList
import com.insperity.escmobile.net.gson.OrgDefaultRequest
import com.insperity.escmobile.net.gson.PunchList
import com.insperity.escmobile.net.gson.Response
import com.insperity.escmobile.net.service.TimePunchService
import com.insperity.escmobile.util.ConnectionUtil
import com.insperity.escmobile.util.Preferences
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
