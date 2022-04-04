package com.delphiaconsulting.timestar.net.service

import com.delphiaconsulting.timestar.net.gson.*
import retrofit2.http.*
import rx.Observable

interface TimeOffService {

    @GET("Accruals/Summary/{employeeId}")
    fun getSummary(@Header("Authorization") token: String?, @Path("employeeId") employeeId: Int): Observable<Response<TimeOffSummary>>

    @GET("Accruals/Metadata/{employeeId}")
    fun getBalancesMetadata(@Header("Authorization") token: String?, @Path("employeeId") employeeId: Int): Observable<Response<TimeOffBalancesMeta>>

    @GET("Accruals/Balances/{employeeId}")
    fun getBalances(@Header("Authorization") token: String?, @Path("employeeId") employeeId: Int, @Query("dates[]") vararg dates: String): Observable<Response<TimeOffBalances>>

    @GET("Request/TimeOff/Init")
    fun getTimeOffRequestsMetadata(@Header("Authorization") token: String?): Observable<Response<TimeOffRequestsMeta>>

    @GET("Request/SentByMe/ListView")
    fun getTimeOffRequests(@Header("Authorization") token: String?): Observable<Response<TimeOffRequests>>

    @GET("Request/{requestId}")
    fun getTimeOffRequest(@Header("Authorization") token: String?, @Path("requestId") requestId: String): Observable<Response<TimeOffRequestDetails>>

    @POST("Request/TimeOff/Submit")
    fun submitTimeOffRequest(@Header("Authorization") token: String?, @Body request: TimeOffSubmitRequest): Observable<Response<TimeOffSubmitResponse>>

    @GET("Request/SentToMe/ListView?showHidden=1")
    fun getTimeOffRequestsReview(@Header("Authorization") token: String?): Observable<Response<TimeOffApprovalRequests>>

    @POST("Request/{requestId}/Respond")
    fun respondTimeOffRequest(@Header("Authorization") token: String?, @Path("requestId") requestId: String, @Body timeOffRequestResolution: TimeOffRequestResolution): Observable<Response<TimeOffResolutionResult>>
}
