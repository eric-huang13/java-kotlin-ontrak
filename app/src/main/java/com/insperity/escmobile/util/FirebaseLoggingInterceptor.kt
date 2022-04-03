package com.insperity.escmobile.util

import com.crashlytics.android.Crashlytics
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.*

/**
 * Created by dxsier on 3/7/17.
 */

class FirebaseLoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestTime = System.nanoTime()
        Crashlytics.log(String.format(Locale.US, "--> Request to URL: %s (%s) -- HEADERS: %n%s", request.url.toString(), request.method.toString(), request.headers.toString()))
        val response = chain.proceed(request)
        val responseTime = System.nanoTime()
        Crashlytics.log(String.format(Locale.US, "<-- Response for URL: %s (%.1fms) (%d) -- HEADERS: %n%s", response.request.url.toString(), (responseTime - requestTime) / 1e6, response.code.toString(), response.headers.toString()))
        return response
    }
}