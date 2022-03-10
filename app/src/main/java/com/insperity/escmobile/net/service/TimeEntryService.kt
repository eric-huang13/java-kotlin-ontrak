package com.insperity.escmobile.net.service

import com.insperity.escmobile.net.gson.*
import retrofit2.http.*
import rx.Observable

interface TimeEntryService {

    @GET("PayPeriodDates/List")
    fun getPayPeriods(@Header("Authorization") token: String, @Query("employeeId") employeeId: Int?): Observable<Response<PayPeriodList>>

    @GET("TotalHours")
    fun getTotalHours(@Header("Authorization") token: String, @Query("payPeriodId") payPeriodId: String, @Query("employeeId") employeeId: Int?): Observable<Response<TotalHoursList>>

    @GET("Hours/List")
    fun getHours(@Header("Authorization") token: String, @Query("payPeriodId") payPeriodId: String, @Query("employeeId") employeeId: Int?): Observable<Response<HoursList>>

    @GET("Punch/List")
    fun getPunches(@Header("Authorization") token: String, @Query("payPeriodId") payPeriodId: String, @Query("employeeId") employeeId: Int?): Observable<Response<PunchList>>

    @GET("Dollars/List")
    fun getDollars(@Header("Authorization") token: String, @Query("payPeriodId") payPeriodId: String, @Query("employeeId") employeeId: Int?): Observable<Response<DollarList>>

    @POST("TotalHours/Approve")
    fun approveTime(@Header("Authorization") token: String, @Body request: TimeApproveRequest): Observable<Response<TimeApprovalStatus>>

    @GET("Approval/Employee/List")
    fun getEmployeeList(@Header("Authorization") token: String): Observable<Response<EmployeeList>>

    @POST("Approval/Status")
    fun getEmployeeBatch(@Header("Authorization") token: String, @Body employeeBatchRequest: EmployeeBatchRequest): Observable<Response<EmployeeBatch>>
}