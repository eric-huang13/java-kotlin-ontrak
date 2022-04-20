package com.delphiaconsulting.timestar.store.impl

import com.crashlytics.android.Crashlytics
import com.delphiaconsulting.timestar.action.Action
import com.delphiaconsulting.timestar.action.Actions
import com.delphiaconsulting.timestar.action.Keys
import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.event.OnAuthTokenReceived
import com.delphiaconsulting.timestar.event.OnSessionDataReceived
import com.delphiaconsulting.timestar.event.OnTokenOrSessionDataError
import com.delphiaconsulting.timestar.net.analytics.AnalyticsDimensions
import com.delphiaconsulting.timestar.net.analytics.Tracker
import com.delphiaconsulting.timestar.net.gson.ErrorMessage
import com.delphiaconsulting.timestar.net.gson.Response
import com.delphiaconsulting.timestar.net.gson.SessionData
import com.delphiaconsulting.timestar.store.SessionStore
import com.delphiaconsulting.timestar.store.Store
import com.delphiaconsulting.timestar.util.Preferences
import com.delphiaconsulting.timestar.util.PunchMode
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

/**
 * Created by dxsier on 12/27/16.
 */

@Singleton
class SessionStoreImpl(dispatcher: Dispatcher, bus: EventBus, private val preferences: Preferences, private val tracker: Tracker) : Store(dispatcher, bus), SessionStore {

    companion object {
        private const val TIME_OFF_APPROVAL_KEY = "RQAPPV"
        private const val TIME_OFF_ACCRUALS_KEY = "ACCRLS"
        private const val TIME_OFF_REQUEST_KEY = "RQUSTS"
        private const val OFFLINE_PUNCH_KEY = "OLPNCH"
        private const val ONLINE_PUNCH_KEY = "MBPNCH"
        private const val TIME_ENTRY_KEY = "EMPTIM"
        private const val TIME_MANAGEMENT_KEY = "SUPTIM"
    }

    //@Suppress("UNCHECKED_CAST")
    override fun onActionReceived(action: Action) {
        when (action.type) {
            Actions.AUTH_TOKEN_RECEIVED -> {
                val token = action.getByKey(Keys.AUTH_TOKEN) as String
                preferences.timeStarToken = token
                emitChange(OnAuthTokenReceived())
            }
            Actions.AUTH_TOKEN_ERROR -> {
                val errorMessage = action.getByKey(Keys.ERROR) as ErrorMessage?
                emitChange(OnTokenOrSessionDataError(errorMessage?.message))
            }
            Actions.SESSION_DATA_RECEIVED -> {
                val sessionData = action.getByKey(Keys.SESSION_DATA) as Response<SessionData>?
                if (sessionData?.data != null) {
                    tracker.trackDimension(AnalyticsDimensions.CLIENT_ID, sessionData.data.clientId.toString())
                    tracker.trackDimension(AnalyticsDimensions.COMPANY_ID, sessionData.data.companyId.toString())
                    tracker.trackDimension(AnalyticsDimensions.USER_ID, sessionData.data.sessionUserId.toString())
                    Crashlytics.setUserIdentifier(sessionData.data.sessionEmployeeId.toString())
                    preferences.sessionEmployeeId = sessionData.data.sessionEmployeeId
                    preferences.timeOffBalancesEnabled = sessionData.data.features.contains(TIME_OFF_ACCRUALS_KEY)
                    preferences.timeOffRequestEnabled = sessionData.data.features.contains(TIME_OFF_REQUEST_KEY)
                    preferences.timeOffApprovalEnabled = sessionData.data.features.contains(TIME_OFF_APPROVAL_KEY)
                    preferences.timeEntryEnabled = sessionData.data.features.contains(TIME_ENTRY_KEY)
                    preferences.timeManagementEnabled = sessionData.data.features.contains(TIME_MANAGEMENT_KEY)
                    preferences.punchMode = when {
                        sessionData.data.features.contains(OFFLINE_PUNCH_KEY) -> PunchMode.PUNCH_OFFLINE_MODE
                        sessionData.data.features.contains(ONLINE_PUNCH_KEY) -> PunchMode.PUNCH_ONLINE_MODE
                        else -> PunchMode.NO_PUNCH_MODE
                    }
                    emitChange(OnSessionDataReceived(preferences.punchMode == PunchMode.NO_PUNCH_MODE))
                    return
                }
                emitChange(OnTokenOrSessionDataError(sessionData?.message?.message))
            }
            Actions.TOKEN_OR_SESSION_DATA_ERROR -> {
                if (handleCommonError(action)) return
                emitChange(OnTokenOrSessionDataError(null))
            }
            else -> logOnActionNotCaught(action.type)
        }
    }
}
