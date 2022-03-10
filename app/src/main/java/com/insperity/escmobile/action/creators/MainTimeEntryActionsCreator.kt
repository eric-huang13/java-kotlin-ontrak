package com.insperity.escmobile.action.creators

import com.insperity.escmobile.R
import com.insperity.escmobile.action.Actions
import com.insperity.escmobile.action.Keys
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.net.gson.*
import com.insperity.escmobile.net.service.TimeEntryService
import com.insperity.escmobile.util.Preferences
import com.insperity.escmobile.util.TimeEntryUtil.LOAD_BATCH_SIZE
import com.insperity.escmobile.view.common.TimeEntryEmployee
import rx.Observable

/**
 * Created by dxsier on 2/20/18.
 */

@Suppress("UNCHECKED_CAST")
abstract class MainTimeEntryActionsCreator constructor(dispatcher: Dispatcher, taskCache: Any?, private val timeEntryService: TimeEntryService, protected var preferences: Preferences) : ActionsCreator(dispatcher, taskCache) {

    fun getPayPeriods(employeeId: Int? = null) = executeTask(timeEntryService.getPayPeriods(preferences.timeStarToken, employeeId),
            { dispatcher.dispatch(Actions.TIME_ENTRY_PAY_PERIODS_RECEIVED, Keys.TIME_ENTRY_PAY_PERIODS, it) },
            { dispatcher.dispatch(Actions.TIME_ENTRY_ERROR, Keys.ERROR, it) })

    fun getTotalHours(payPeriodId: String, employeeId: Int? = null, cascade: Boolean = true) =
            executeTask(timeEntryService.getTotalHours(preferences.timeStarToken, payPeriodId, employeeId),
                    {
                        dispatcher.dispatch(Actions.TIME_ENTRY_TOTAL_HOURS_RECEIVED, Keys.TIME_ENTRY_TOTAL_HOURS, it)
                        if (cascade) {
                            val access = (it as Response<TotalHoursList>).data?.referenceData?.access
                            if (access?.hours == true) getHours(payPeriodId, employeeId)
                            if (access?.punches == true) getPunches(payPeriodId, employeeId)
                            if (access?.dollars == true) getDollars(payPeriodId, employeeId)
                        }
                    },
                    { dispatcher.dispatch(Actions.TIME_ENTRY_ERROR, Keys.ERROR, it) })


    private fun getHours(payPeriodId: String, employeeId: Int? = null) =
            executeTask(timeEntryService.getHours(preferences.timeStarToken, payPeriodId, employeeId),
                    { dispatcher.dispatch(Actions.TIME_ENTRY_HOURS_RECEIVED, Keys.TIME_ENTRY_HOURS, it) },
                    { dispatcher.dispatch(Actions.TIME_ENTRY_ERROR, Keys.ERROR, it) })

    private fun getPunches(payPeriodId: String, employeeId: Int? = null) =
            executeTask(timeEntryService.getPunches(preferences.timeStarToken, payPeriodId, employeeId),
                    { dispatcher.dispatch(Actions.TIME_ENTRY_PUNCHES_RECEIVED, Keys.TIME_ENTRY_PUNCHES, it) },
                    { dispatcher.dispatch(Actions.TIME_ENTRY_ERROR, Keys.ERROR, it) })

    private fun getDollars(payPeriodId: String, employeeId: Int? = null) =
            executeTask(timeEntryService.getDollars(preferences.timeStarToken, payPeriodId, employeeId),
                    { dispatcher.dispatch(Actions.TIME_ENTRY_DOLLARS_RECEIVED, Keys.TIME_ENTRY_DOLLARS, it) },
                    { dispatcher.dispatch(Actions.TIME_ENTRY_ERROR, Keys.ERROR, it) })

    fun approveTime(employeeId: Int?, request: TimeApproveRequest) =
            executeTask(timeEntryService.approveTime(preferences.timeStarToken, request),
                    {
                        dispatcher.dispatch(Actions.TIME_ENTRY_APPROVE_TIME_RESULT, Keys.TIME_ENTRY_APPROVE_TIME_RESULT, it)
                        if ((it as Response<TimeApprovalStatus>).data?.content?.results?.firstOrNull { it.employeeId == request.approvalRequests[0].employeeId }?.isSuccess == true) {
                            getTotalHours(request.payPeriodId, employeeId, false)
                        }
                    },
                    { dispatcher.dispatch(Actions.TIME_ENTRY_ERROR, Keys.ERROR, it) })

    fun getEmployeeList() = executeTask(timeEntryService.getEmployeeList(preferences.timeStarToken),
            { dispatcher.dispatch(Actions.TIME_ENTRY_EMPLOYEES_RECEIVED, Keys.TIME_ENTRY_EMPLOYEES, it) },
            { dispatcher.dispatch(Actions.TIME_ENTRY_ERROR, Keys.ERROR, it) })

    fun loadEmployees(employees: List<EmployeeList.Employee>, loadingPayGroupId: Int) = executeTask(Observable.from(employees)
            .map { it.employeeId }
            .compose { it -> batchObservable(it, R.string.loading_x_out_of_y_text, employees.size, loadingPayGroupId) { dispatcher.dispatch(Actions.TIME_ENTRY_EMPLOYEE_BATCH_RECEIVED, Keys.TIME_ENTRY_EMPLOYEE_BATCH_DATA, it, Keys.TIME_ENTRY_EMPLOYEE_BATCH, employees, Keys.TIME_ENTRY_LOADING_PAY_GROUP_ID, loadingPayGroupId) } },
            {}, { dispatcher.dispatch(Actions.TIME_ENTRY_ERROR, Keys.ERROR, it) })

    fun loadEmployee(employee: TimeEntryEmployee, loadingPayGroupId: Int, error: String) = executeTask(Observable.just(employee)
            .map { it.employeeId }
            .compose { it -> batchObservable(it, R.string.loading_x_out_of_y_text, 1, loadingPayGroupId) {} },
            { dispatcher.dispatch(Actions.TIME_ENTRY_EMPLOYEE_LOADED, Keys.TIME_ENTRY_EMPLOYEE_DATA, it, Keys.TIME_ENTRY_EMPLOYEE, employee, Keys.TIME_ENTRY_LOADING_PAY_GROUP_ID, loadingPayGroupId, Keys.TIME_ENTRY_EMPLOYEE_ERROR, error) },
            { dispatcher.dispatch(Actions.TIME_ENTRY_ERROR, Keys.ERROR, it) })

    fun massApproveTime(request: TimeApproveRequest, selectedEmployees: List<TimeEntryEmployee>, loadingPayGroupId: Int, updateTextRes: Int) = executeTask(timeEntryService.approveTime(preferences.timeStarToken, request)
            .switchMap { approvalResponse ->
                if (approvalResponse.message != null) {
                    dispatcher.dispatch(Actions.TIME_ENTRY_MASS_APPROVE_TIME_RESULT, Keys.TIME_ENTRY_APPROVE_TIME_RESULT, approvalResponse)
                    return@switchMap Observable.empty<Response<TimeApprovalStatus>>()
                }
                return@switchMap Observable.from(approvalResponse.data.content.results)
                        .map { it.employeeId }
                        .compose { it -> batchObservable(it, updateTextRes, selectedEmployees.size, loadingPayGroupId) {} }
                        .map { Pair(approvalResponse, it) }
            },
            { dispatcher.dispatch(Actions.TIME_ENTRY_MASS_APPROVE_TIME_RESULT, Keys.TIME_ENTRY_APPROVE_TIME_RESULT, (it as Pair<Any, Any>).first, Keys.TIME_ENTRY_EMPLOYEE_BATCH, it.second, Keys.TIME_ENTRY_SELECTED_EMPLOYEES, selectedEmployees, Keys.TIME_ENTRY_LOADING_PAY_GROUP_ID, loadingPayGroupId) },
            { dispatcher.dispatch(Actions.TIME_ENTRY_ERROR, Keys.ERROR, it) })

    private fun batchObservable(observable: Observable<Int>, updateTextRes: Int, totalAmount: Int, loadingPayGroupId: Int, dispatchBatch: (EmployeeBatch) -> Unit): Observable<MutableList<EmployeeBatch.ApprovalStatus>> = observable
            .buffer(LOAD_BATCH_SIZE)
            .concatMap { timeEntryService.getEmployeeBatch(preferences.timeStarToken, EmployeeBatchRequest(it)) }
            .filter { it.data != null }
            .map { it.data }
            .reduce(mutableListOf()) { accumulator: MutableList<EmployeeBatch.ApprovalStatus>, batch: EmployeeBatch ->
                accumulator.addAll(batch.content.approvalStatus)
                dispatcher.dispatch(Actions.TIME_ENTRY_MASS_APPROVE_PROGRESS_UPDATE, Keys.UPDATE_TEXT_RES, updateTextRes, Keys.LOADED_AMOUNT, accumulator.size, Keys.TOTAL_AMOUNT, totalAmount, Keys.TIME_ENTRY_LOADING_PAY_GROUP_ID, loadingPayGroupId)
                dispatchBatch(batch)
                return@reduce accumulator
            }
}
