package com.insperity.escmobile.util

import android.content.Context

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by dxsier on 11/18/16.
 */

@Singleton
class Preferences @Inject constructor(context: Context) : MainPreferences(context.getSharedPreferences("ontrak_preferences", Context.MODE_PRIVATE)) {

    companion object {
        private const val SESSION_EMPLOYEE_ID = "SESSION_EMPLOYEE_ID"
        private const val TIME_OFF_APPROVAL_ENABLED_PREF = "com.insperity.escmobile.TIME_OFF_APPROVAL_ENABLED_PREF"
        private const val TIME_ENTRY_ENABLED_PREF = "com.insperity.escmobile.TIME_ENTRY_ENABLED_PREF"
        private const val TIME_MANAGEMENT_ENABLED_PREF = "com.insperity.escmobile.TIME_MANAGEMENT_ENABLED_PREF"
    }

    override var timeStarToken: String
        get() = TokenUtil.formatItaAuthToken(super.timeStarToken)
        set(token) {
            super.timeStarToken = token
        }

    var sessionEmployeeId: Int
        get() = sharedPref.getInt(SESSION_EMPLOYEE_ID, -1)
        set(sessionEmployeeId) = sharedPref.edit().putInt(SESSION_EMPLOYEE_ID, sessionEmployeeId).apply()

    var timeOffApprovalEnabled: Boolean
        get() = sharedPref.getBoolean(TIME_OFF_APPROVAL_ENABLED_PREF, false)
        set(available) = sharedPref.edit().putBoolean(TIME_OFF_APPROVAL_ENABLED_PREF, available).apply()

    var timeEntryEnabled: Boolean
        get() = sharedPref.getBoolean(TIME_ENTRY_ENABLED_PREF, false)
        set(available) = sharedPref.edit().putBoolean(TIME_ENTRY_ENABLED_PREF, available).apply()

    var timeManagementEnabled: Boolean
        get() = sharedPref.getBoolean(TIME_MANAGEMENT_ENABLED_PREF, false)
        set(available) = sharedPref.edit().putBoolean(TIME_MANAGEMENT_ENABLED_PREF, available).apply()
}
