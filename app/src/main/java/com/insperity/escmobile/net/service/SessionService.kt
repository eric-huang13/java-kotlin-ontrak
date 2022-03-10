package com.insperity.escmobile.net.service

import com.insperity.escmobile.net.gson.RegistrationRequest
import com.insperity.escmobile.net.gson.RegistrationToken
import com.insperity.escmobile.net.gson.Response
import com.insperity.escmobile.net.gson.SessionData

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import rx.Observable

interface SessionService {

    @POST("api/Onboarding/DeviceRegistration")
    fun registerDevice(@Body registrationData: RegistrationRequest): Observable<Response<RegistrationToken>>

    @GET("Session/Metadata")
    fun sessionData(@Header("Authorization") token: String?): Observable<Response<SessionData>>
}