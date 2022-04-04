package com.delphiaconsulting.timestar.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager

import javax.inject.Singleton

/**
 * Created by dxsier on 1/6/17.
 */

@Singleton
class ConnectionUtil(private val context: Context) {

    private val networkInfo: NetworkInfo?
        get() {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo
        }

    val isConnected: Boolean
        get() {
            val info = networkInfo
            return info != null && info.isAvailable && info.isConnected
        }

    val isConnectedWifi: Boolean
        get() {
            val info = networkInfo
            return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
        }

    val isConnectedMobile: Boolean
        get() {
            val info = networkInfo
            return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_MOBILE
        }

    val isConnectedFast: Boolean
        get() {
            val info = networkInfo
            return info != null && info.isConnected && isConnectionFast(info.type, info.subtype)
        }

    private fun isConnectionFast(type: Int, subType: Int): Boolean = when (type) {
        ConnectivityManager.TYPE_WIFI -> true
        ConnectivityManager.TYPE_MOBILE -> when (subType) {
            TelephonyManager.NETWORK_TYPE_1xRTT -> false // ~ 50-100 kbps
            TelephonyManager.NETWORK_TYPE_CDMA -> false // ~ 14-64 kbps
            TelephonyManager.NETWORK_TYPE_EDGE -> false // ~ 50-100 kbps
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> true // ~ 400-1000 kbps
            TelephonyManager.NETWORK_TYPE_EVDO_A -> true // ~ 600-1400 kbps
            TelephonyManager.NETWORK_TYPE_GPRS -> false // ~ 100 kbps
            TelephonyManager.NETWORK_TYPE_HSDPA -> true // ~ 2-14 Mbps
            TelephonyManager.NETWORK_TYPE_HSPA -> true // ~ 700-1700 kbps
            TelephonyManager.NETWORK_TYPE_HSUPA -> true // ~ 1-23 Mbps
            TelephonyManager.NETWORK_TYPE_UMTS -> true // ~ 400-7000 kbps
            TelephonyManager.NETWORK_TYPE_EHRPD -> true // ~ 1-2 Mbps // API level 11
            TelephonyManager.NETWORK_TYPE_EVDO_B -> true // ~ 5 Mbps // API level 9
            TelephonyManager.NETWORK_TYPE_HSPAP -> true // ~ 10-20 Mbps // API level 13
            TelephonyManager.NETWORK_TYPE_IDEN -> false // ~25 kbps // API level 8
            TelephonyManager.NETWORK_TYPE_LTE -> true // ~ 10+ Mbps // API level 11
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> false // Unknown
            else -> false
        }
        else -> false
    }
}
