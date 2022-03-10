package com.insperity.escmobile.net.service

import com.insperity.escmobile.net.gson.*
import retrofit2.http.*
import rx.Observable

interface TimePunchService {

    @GET("ServerTime")
    fun getServerTime(@Header("Authorization") token: String?): Observable<Response<ServerTime>>

    @GET("Punch/Metadata")
    fun getPunchCategories(@Header("Authorization") token: String?): Observable<Response<PunchCategoryList>>

    @GET("OrgLevels/Items")
    fun getOrgLevels(@Header("Authorization") token: String?): Observable<Response<OrgLevelList>>

    @GET("OrgLevels/Items/Hierarchy")
    fun getOrgHierarchy(@Header("Authorization") token: String?): Observable<Response<OrgHierarchyList>>

    @POST("OrgLevels/Items/Hierarchy/Search")
    fun getOrgMainDefault(@Header("Authorization") token: String?, @Body orgDefaultRequest: OrgDefaultRequest): Observable<Response<OrgDefaultList>>

    @GET("OrgLevels/Items/Defaults")
    fun getOrgDefaults(@Header("Authorization") token: String?): Observable<Response<OrgDefaultList>>

    @GET("Punch/List")
    fun getPunches(@Header("Authorization") token: String?, @Query("employeeId") employeeId: Int, @Query("startDate") startDate: String, @Query("stopDate") stopDate: String): Observable<Response<PunchList>>

    @POST("Punch/Submit")
    fun submitPunch(@Header("Authorization") token: String?, @Body punchToSubmit: PunchToSubmit): Observable<Response<SubmittedPunch>>

    @POST("Punch/Submit/Offline")
    fun submitOfflinePunches(@Header("Authorization") token: String?, @Body punchesToSubmit: PunchesToSubmit): Observable<Response<SubmittedPunch>>
}