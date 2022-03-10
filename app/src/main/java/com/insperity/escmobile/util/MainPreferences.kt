package com.insperity.escmobile.util

import android.content.SharedPreferences

import com.insperity.escmobile.util.PunchMode.NO_PUNCH_MODE

/**
 * Created by dxsier on 11/18/16.
 */

open class MainPreferences(protected val sharedPref: SharedPreferences) {

    companion object {
        private const val SERVER_TIME_SET_TIME = "SERVER_TIME_SET_TIME"
        private const val SERVER_TIME = "SERVER_TIME"
        private const val TIME_STAR_TOKEN = "TIME_STAR_TOKEN"
        private const val ALLOW_ORG_LEVEL_DEFAULTS_SWITCH = "ALLOW_ORG_LEVEL_DEFAULTS_SWITCH"
        private const val TIME_PUNCH_MODE = "com.insperity.escmobile.TIME_PUNCH_MODE"
        private const val EMPLOYEE_ID_PREF = "com.insperity.escmobile.EMPLOYEE_ID_PREF"
        private const val ORG_LEVEL_SELECTION_PREF = "com.insperity.escmobile.ORG_LEVEL_SELECTION_PREF"
        private const val TIME_OFF_BALANCES_ENABLED_PREF = "com.insperity.escmobile.TIME_OFF_BALANCES_ENABLED_PREF"
        private const val TIME_OFF_REQUEST_ENABLED_PREF = "com.insperity.escmobile.TIME_OFF_REQUEST_ENABLED_PREF"
    }

    open var timeStarToken: String
        get() = sharedPref.getString(TIME_STAR_TOKEN, "") ?: ""
        set(token) = sharedPref.edit().putString(TIME_STAR_TOKEN, token).apply()

    open var serverTime: Long
        get() = sharedPref.getLong(SERVER_TIME, 0)
        set(timeMillis) = sharedPref.edit().putLong(SERVER_TIME, timeMillis).apply()

    var serverTimeSetTime: Long
        get() = sharedPref.getLong(SERVER_TIME_SET_TIME, 0)
        set(timeMillis) = sharedPref.edit().putLong(SERVER_TIME_SET_TIME, timeMillis).apply()

    var allowOrgLevelDefaultsSwitch: Boolean
        get() = sharedPref.getBoolean(ALLOW_ORG_LEVEL_DEFAULTS_SWITCH, true)
        set(allow) = sharedPref.edit().putBoolean(ALLOW_ORG_LEVEL_DEFAULTS_SWITCH, allow).apply()

    open var punchMode: Int
        get() = sharedPref.getInt(TIME_PUNCH_MODE, NO_PUNCH_MODE)
        set(@PunchMode mode) = sharedPref.edit().putInt(TIME_PUNCH_MODE, mode).apply()

    open var employeeId: Int
        get() = sharedPref.getInt(EMPLOYEE_ID_PREF, 0)
        set(employeeId) = sharedPref.edit().putInt(EMPLOYEE_ID_PREF, employeeId).apply()

    var quickOrgLevelSelection: Boolean
        get() = sharedPref.getBoolean(ORG_LEVEL_SELECTION_PREF, true)
        set(quickSelection) = sharedPref.edit().putBoolean(ORG_LEVEL_SELECTION_PREF, quickSelection).apply()

    open var timeOffBalancesEnabled: Boolean
        get() = sharedPref.getBoolean(TIME_OFF_BALANCES_ENABLED_PREF, false)
        set(enabled) = sharedPref.edit().putBoolean(TIME_OFF_BALANCES_ENABLED_PREF, enabled).apply()

    open var timeOffRequestEnabled: Boolean
        get() = sharedPref.getBoolean(TIME_OFF_REQUEST_ENABLED_PREF, false)
        set(enabled) = sharedPref.edit().putBoolean(TIME_OFF_REQUEST_ENABLED_PREF, enabled).apply()
}
