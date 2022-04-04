package com.delphiaconsulting.timestar.net.service

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url
import rx.Observable

interface LegalService {

    @Headers("Accept:text/html")
    @GET
    fun getLegal(@Url url: String): Observable<String>
}