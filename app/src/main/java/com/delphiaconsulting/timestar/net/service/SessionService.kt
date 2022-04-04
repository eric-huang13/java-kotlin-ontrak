package com.delphiaconsulting.timestar.net.service

import com.delphiaconsulting.timestar.net.gson.RegistrationRequest
import com.delphiaconsulting.timestar.net.gson.RegistrationToken
import com.delphiaconsulting.timestar.net.gson.Response
import com.delphiaconsulting.timestar.net.gson.SessionData

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