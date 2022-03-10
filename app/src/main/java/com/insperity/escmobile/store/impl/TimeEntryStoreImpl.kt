package com.insperity.escmobile.store.impl

import android.content.Context
import com.insperity.escmobile.R
import com.insperity.escmobile.action.Action
import com.insperity.escmobile.action.Actions
import com.insperity.escmobile.action.Keys
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.event.*
import com.insperity.escmobile.net.gson.*
import com.insperity.escmobile.store.Store
import com.insperity.escmobile.store.TimeEntryStore
import com.insperity.escmobile.util.AppUtil
import com.insperity.escmobile.util.StringUtil.timeWithFormat
import com.insperity.escmobile.util.TimeEntryUtil.SUPERVISOR_CAN_APPROVE_ARRAY
import com.insperity.escmobile.util.TimeEntryUtil.SUPERVISOR_CAN_UNAPPROVE_ARRAY
import com.insperity.escmobile.util.TimeEntryUtil.SUPERVISOR_EMP_APP_DISABLED_ARRAY
import com.insperity.escmobile.view.common.*
import com.insperity.escmobile.view.extension.partOf
import org.greenrobot.eventbus.EventBus
import org.joda.time.format.DateTimeFormat
import rx.Observable
import rx.subjects.PublishSubject
import java.text.DecimalFormat
import javax.inject.Singleton

/**
 * Created by dxsier on 2/20/18.
 */
@Singleton
class TimeEntryStoreImpl(dispatcher: Dispatcher, bus: EventBus, val context: Context, val appUtil: AppUtil) : Store(dispatcher, bus), TimeEntryStore {

    companion object {
        const val HOURS_FLOAT = "HOURS_FLOAT"
    }

    override val onEmployeeLoadingProgressUpdateSubject: PublishSubject<OnEmployeeLoadingProgressUpdate> = PublishSubject.create()
    override val onEmployeeBatchProcessedSubject: PublishSubject<OnEmployeeBatchProcessed> = PublishSubject.create()

    @Suppress("UNCHECKED_CAST")
    override fun onActionReceived(action: Action) {
        when (action.type) {
            Actions.TIME_ENTRY_PAY_PERIODS_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_ENTRY_PAY_PERIODS) as Response<PayPeriodList>
                if (onServiceError(response.message)) return
                processPayPeriods(response.data)
            }
            Actions.TIME_ENTRY_TOTAL_HOURS_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_ENTRY_TOTAL_HOURS) as Response<TotalHoursList>
                if (onServiceError(response.message)) {
                    emitChange(OnTimeEntryTotalsReceived("0h", "0h", "0h"))
                    emitStickyChange(OnTimeEntrySummaryReceived(arrayListOf(), arrayListOf()))
                    emitStickyChange(OnTimeEntryDetailsReceived(arrayListOf(), arrayListOf()))
                    emitStickyChange(OnTimeEntryPunchesReceived(arrayListOf(), arrayListOf()))
                    emitStickyChange(OnTimeEntryHoursReceived(arrayListOf(), arrayListOf()))
                    emitStickyChange(OnTimeEntryDollarsReceived(arrayListOf(), arrayListOf()))
                    return
                }
                processCardTotals(response.data.content.employeeCalcTime, response.data.referenceData.timeFormat == HOURS_FLOAT)
                emitChange(OnApprovalMaskStatusReceived(response.data.referenceData.approvalStatus.statusFlags))
                emitChange(OnBottomTabAccessReceived(response.data.referenceData.access))
                if (response.data.content.employeeCalcTime.isEmpty()) {
                    emitStickyChange(OnTimeEntrySummaryReceived(arrayListOf(), arrayListOf()))
                    emitStickyChange(OnTimeEntryDetailsReceived(arrayListOf(), arrayListOf()))
                    return
                }
                processTotalHoursSummary(response.data.content.employeeCalcTime, decimalFormatted = response.data.referenceData.timeFormat == HOURS_FLOAT)
                processTotalHoursDetails(response.data.content.employeeCalcTime, decimalFormatted = response.data.referenceData.timeFormat == HOURS_FLOAT)
            }
            Actions.TIME_ENTRY_HOURS_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_ENTRY_HOURS) as Response<HoursList>
                if (onServiceError(response.message)) {
                    emitStickyChange(OnTimeEntryHoursReceived(arrayListOf(), arrayListOf()))
                    return
                }
                processHours(response.data, response.data.referenceData.timeFormat == HOURS_FLOAT)
            }
            Actions.TIME_ENTRY_PUNCHES_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_ENTRY_PUNCHES) as Response<PunchList>
                if (onServiceError(response.message)) {
                    emitStickyChange(OnTimeEntryPunchesReceived(arrayListOf(), arrayListOf()))
                    return
                }
                processPunches(response.data)
            }
            Actions.TIME_ENTRY_DOLLARS_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_ENTRY_DOLLARS) as Response<DollarList>
                if (onServiceError(response.message)) {
                    emitStickyChange(OnTimeEntryDollarsReceived(arrayListOf(), arrayListOf()))
                    return
                }
                processDollars(response.data)
            }
            Actions.TIME_ENTRY_APPROVE_TIME_RESULT -> {
                val response = action.getByKey(Keys.TIME_ENTRY_APPROVE_TIME_RESULT) as Response<TimeApprovalStatus>
                if (onServiceError(response.message)) return
                if (response.data.content.results.size == 1 && response.data.content.results[0].error != null) {
                    emitChange(OnApprovalTimeEntryDataError(response.data.content.results[0].error?.message))
                    return
                }
                emitChange(OnApprovalTimeEntrySuccessful())
            }
            Actions.TIME_ENTRY_EMPLOYEES_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_ENTRY_EMPLOYEES) as Response<EmployeeList>
                if (onServiceError(response.message)) return
                val accessMask = response.data.accessFlags.approvalAccessFlags
                emitChange(OnSupervisorAccessFlagsReceived(accessMask.partOf(SUPERVISOR_CAN_APPROVE_ARRAY), accessMask.partOf(SUPERVISOR_CAN_UNAPPROVE_ARRAY), accessMask.partOf(SUPERVISOR_EMP_APP_DISABLED_ARRAY), response.data.accessFlags.dollarsFlag != 1))
                processPayGroupsWithEmployeeLists(response.data)
            }
            Actions.TIME_ENTRY_EMPLOYEE_BATCH_RECEIVED -> {
                val employeeBatch = action.getByKey(Keys.TIME_ENTRY_EMPLOYEE_BATCH_DATA) as EmployeeBatch
                val loadedEmployees = action.getByKey(Keys.TIME_ENTRY_EMPLOYEE_BATCH) as List<EmployeeList.Employee>
                val loadingPayGroupId = action.getByKey(Keys.TIME_ENTRY_LOADING_PAY_GROUP_ID) as Int
                processLoadedEmployees(employeeBatch, loadedEmployees, loadingPayGroupId)
            }
            Actions.TIME_ENTRY_EMPLOYEE_LOADED -> {
                val statusList = action.getByKey(Keys.TIME_ENTRY_EMPLOYEE_DATA) as List<EmployeeBatch.ApprovalStatus>
                val employee = action.getByKey(Keys.TIME_ENTRY_EMPLOYEE) as TimeEntryEmployee
                val loadingPayGroupId = action.getByKey(Keys.TIME_ENTRY_LOADING_PAY_GROUP_ID) as Int
                val error = action.getByKey(Keys.TIME_ENTRY_EMPLOYEE_ERROR) as String
                processLoadedEmployee(statusList, employee, error, loadingPayGroupId)
            }
            Actions.TIME_ENTRY_MASS_APPROVE_PROGRESS_UPDATE -> onEmployeeLoadingProgressUpdateSubject.onNext(OnEmployeeLoadingProgressUpdate(action.getByKey(Keys.UPDATE_TEXT_RES) as Int, action.getByKey(Keys.LOADED_AMOUNT) as Int, action.getByKey(Keys.TOTAL_AMOUNT) as Int, action.getByKey(Keys.TIME_ENTRY_LOADING_PAY_GROUP_ID) as Int))
            Actions.TIME_ENTRY_MASS_APPROVE_TIME_RESULT -> {
                val approvalResponse = action.getByKey(Keys.TIME_ENTRY_APPROVE_TIME_RESULT) as Response<TimeApprovalStatus>
                if (onServiceError(approvalResponse.message)) return
                val statusListResponse = action.getByKey(Keys.TIME_ENTRY_EMPLOYEE_BATCH) as List<EmployeeBatch.ApprovalStatus>
                val selectedEmployees = action.getByKey(Keys.TIME_ENTRY_SELECTED_EMPLOYEES) as List<TimeEntryEmployee>
                val loadingPayGroupId = action.getByKey(Keys.TIME_ENTRY_LOADING_PAY_GROUP_ID) as Int
                processMassApproval(approvalResponse, statusListResponse, selectedEmployees, loadingPayGroupId)
            }
            Actions.TIME_ENTRY_ERROR -> {
                handleCommonError(action)
            }
            else -> logOnActionNotCaught(action.type)
        }
    }

    private fun onServiceError(message: ErrorMessage?): Boolean {
        if (message != null) {
            emitChange(OnTimeEntryDataError(message.message))
            return true
        }
        return false
    }

    //Pay periods section
    private fun processPayPeriods(payPeriodList: PayPeriodList) {
        val dtf = DateTimeFormat.forPattern("MM/dd/yyyy")
        Observable.from(payPeriodList.content.payPeriods)
                .toSortedList { left, right -> dtf.parseDateTime(left.date.startDate).compareTo(dtf.parseDateTime(right.date.startDate)) }
                .map { attachPayPeriodSections(it, payPeriodList.referenceData.payGroups, payPeriodList.content.currentPeriod.idx) }
                .subscribe {
                    emitChange(OnEmployeeInfoReceived(payPeriodList.referenceData.employee.fullName, payPeriodList.referenceData.employee.employeeNumber, payPeriodList.referenceData.employee.employeeId))
                    emitStickyChange(OnPayPeriodListReceived(it, payPeriodList.content.currentPeriod.idx))
                }
    }

    private fun attachPayPeriodSections(payPeriods: List<PayPeriodList.PayPeriod>, payGroups: List<PayPeriodList.PayGroup>, currentPeriod: String): List<AdapterItem> {
        val adapterItems = ArrayList<AdapterItem>()
        var lastGroupId = -1
        for (payPeriod in payPeriods) {
            if (lastGroupId == -1 || lastGroupId != payPeriod.payGroupId) {
                adapterItems.add(PayPeriodSectionItem(payGroups.firstOrNull { it.payGroupId == payPeriod.payGroupId }?.name))
                lastGroupId = payPeriod.payGroupId
            }
            adapterItems.add(PayPeriodItem(payPeriod, currentPeriod == payPeriod.date.idx))
        }
        return adapterItems
    }

    //Card totals section
    private fun processCardTotals(timeItems: List<TotalHoursList.CalculatedTimeItem>, decimalFormat: Boolean) {
        var hours = 0
        var punches = 0
        for (timeItem in timeItems) {
            if (timeItem.otherHoursId.value.toInt() != 0) {
                hours += timeItem.getMinutes()
            } else if (timeItem.punchId.value.toInt() != 0) {
                punches += timeItem.getMinutes()
            }
        }
        emitChange(OnTimeEntryTotalsReceived(timeWithFormat(hours + punches, decimalFormat, decimalPlaces = 2), timeWithFormat(hours, decimalFormat, decimalPlaces = 2), timeWithFormat(punches, decimalFormat, decimalPlaces = 2)))
    }

    //Total hours summary section
    private fun processTotalHoursSummary(timeItems: List<TotalHoursList.CalculatedTimeItem>, decimalFormatted: Boolean) = Observable.from(timeItems)
            .groupBy { it.effectiveDate.value }
            .concatMap { it.groupBy { it.payType.value } }
            .concatMap { it.groupBy { it.shiftType.value } }
            .concatMap {
                it.reduce { l, r ->
                    l.calcLunchMinutes = (l.getLunchMinutes() + r.getLunchMinutes()).toString()
                    l.calcMinutes = (l.getMinutes() + r.getMinutes()).toString()
                    return@reduce l
                }
            }
            .toList()
            .concatMap { applySmartRounding(it, decimalFormatted) }
            .map { getTotalHoursSummaryColumnConfig(it) }
            .subscribe { emitStickyChange(OnTimeEntrySummaryReceived(it.first, it.second)) }

    private fun getTotalHoursSummaryColumnConfig(timeItems: MutableList<TotalHoursList.CalculatedTimeItem>): Pair<List<TimeEntryColumnData>, List<TimeEntryRowData>> {
        val columnNames = intArrayOf(R.string.eff_date_header_text, R.string.pay_type_header_text, R.string.hours_header_text, R.string.lunch_header_text, R.string.shift_header_text)
        val columnValuesFns: Array<(Any) -> String> = arrayOf({ i -> (i as TotalHoursList.CalculatedTimeItem).effectiveDate.sv }, { i -> (i as TotalHoursList.CalculatedTimeItem).payType.sv },
                { i -> (i as TotalHoursList.CalculatedTimeItem).calcMinutes ?: "0" }, { i -> (i as TotalHoursList.CalculatedTimeItem).calcLunchMinutes ?: "0" }, { i -> (i as TotalHoursList.CalculatedTimeItem).shiftType.sv })
        return generateTableData(columnNames, columnValuesFns, items = timeItems, rightAlignedFn = { it == R.string.hours_header_text || it == R.string.lunch_header_text })
    }

    private fun applySmartRounding(timeItems: MutableList<TotalHoursList.CalculatedTimeItem>, decimalFormatted: Boolean): Observable<MutableList<TotalHoursList.CalculatedTimeItem>> = Observable.from(timeItems)
            .map { Pair(it.getLunchMinutes(), it.getMinutes()) }
            .reduce(Pair(2, 2)) { acc, pair -> Pair(Math.max(acc.first, getDecimalPlaces(pair.first)), Math.max(acc.second, getDecimalPlaces(pair.second))) }
            .concatMap { decimalPlaces ->
                Observable.from(timeItems).map {
                    it.calcLunchMinutes = timeWithFormat(it.getLunchMinutes(), decimalFormatted, decimalPlaces = decimalPlaces.first)
                    it.calcMinutes = timeWithFormat(it.getMinutes(), decimalFormatted, decimalPlaces = decimalPlaces.second)
                    return@map it
                }
            }
            .toList()

    private fun getDecimalPlaces(minutes: Int): Int = DecimalFormat("0.00###").format(minutes.toDouble() / 60).split('.')[1].length

    //Total hours details section
    private fun processTotalHoursDetails(timeItems: List<TotalHoursList.CalculatedTimeItem>, decimalFormatted: Boolean) = Observable.from(timeItems)
            .toList()
            .concatMap { applySmartRounding(it, decimalFormatted) }
            .map { getTotalHoursDetailsColumnConfig(it) }
            .subscribe { emitStickyChange(OnTimeEntryDetailsReceived(it.first, it.second)) }

    private fun getTotalHoursDetailsColumnConfig(timeItems: MutableList<TotalHoursList.CalculatedTimeItem>): Pair<List<TimeEntryColumnData>, List<TimeEntryRowData>> {
        val columnNames = intArrayOf(R.string.eff_date_header_text, R.string.pay_type_header_text, R.string.hours_header_text, R.string.lunch_header_text, R.string.start_time_header_text, R.string.stop_time_header_text, R.string.shift_header_text)
        val columnValuesFns: Array<(Any) -> String> = arrayOf({ i -> (i as TotalHoursList.CalculatedTimeItem).effectiveDate.sv }, { i -> (i as TotalHoursList.CalculatedTimeItem).payType.sv },
                { i -> (i as TotalHoursList.CalculatedTimeItem).calcMinutes ?: "0" }, { i -> (i as TotalHoursList.CalculatedTimeItem).calcLunchMinutes ?: "0" }, { i -> (i as TotalHoursList.CalculatedTimeItem).startTimedate.sv },
                { i -> (i as TotalHoursList.CalculatedTimeItem).stopTimedate.sv }, { i -> (i as TotalHoursList.CalculatedTimeItem).shiftType.sv })
        val itemDetail: (Any) -> TimeEntryDetailItem = { TimeEntryDetailItem(context.getString(R.string.detail_total_hours_header_text), getDetailHoursDetails(it as TotalHoursList.CalculatedTimeItem), "Detail") }
        return generateTableData(columnNames, columnValuesFns, itemDetail, timeItems, rightAlignedFn = { it == R.string.hours_header_text || it == R.string.lunch_header_text })
    }

    private fun getDetailHoursDetails(timeItem: TotalHoursList.CalculatedTimeItem): List<AdapterItem> {
        val items = ArrayList<AdapterItem>()
        val isPunch = timeItem.punchId.value.toIntOrNull() != 0
        if (isPunch && timeItem.startTimedate.security != 0) items.add(TimeEntryItem(context.getString(R.string.start_date_time_header_text), timeItem.startTimedate.value))
        if (timeItem.startType.security != 0) items.add(TimeEntryItem(context.getString(R.string.start_type_header_text), timeItem.startType.value))
        if (isPunch && timeItem.stopTimedate.security != 0) items.add(TimeEntryItem(context.getString(R.string.stop_date_time_header_text), timeItem.stopTimedate.value))
        if (timeItem.stopType.security != 0) items.add(TimeEntryItem(context.getString(R.string.stop_type_header_text), timeItem.stopType.value))
        if (timeItem.effectiveDate.security != 0) items.add(TimeEntryItem(context.getString(R.string.effective_date_header_text), timeItem.effectiveDate.value))
        if (timeItem.shiftType.security != 0) items.add(TimeEntryItem(context.getString(R.string.shift_type_header_text), timeItem.shiftType.value))
        if (timeItem.payType.security != 0) items.add(TimeEntryItem(context.getString(R.string.pay_type_header_text), timeItem.payType.value))
        if (timeItem.payRate.security != 0) items.add(TimeEntryItem(context.getString(R.string.pay_rate_header_text), timeItem.payRate.value))
        if (timeItem.minutes.security != 0) items.add(TimeEntryItem(context.getString(R.string.hours_header_text), timeItem.calcMinutes ?: "0"))
        return items
    }

    //Hours section
    private fun processHours(hoursList: HoursList, decimalFormat: Boolean) = Observable.from(hoursList.content.hours)
            .map {
                val payType = hoursList.referenceData.optionLists.payType[it.payType.optionListIndex ?: 0].firstOrNull { payType -> payType.value == it.payType.value }
                val deviceNum = hoursList.referenceData.optionLists.deviceNum[it.deviceNum.optionListIndex ?: 0].firstOrNull { deviceNum -> deviceNum.value == it.deviceNum.value }
                HoursList.Hours(it.comment, ITAFieldAttr(deviceNum?.label ?: "", it.deviceNum.security, it.deviceNum.optionListIndex, it.deviceNum.rules), it.otherHoursId, it.modifiedFlag, it.actualDate,
                        ITAFieldAttr(payType?.label ?: "", it.payType.security, it.payType.optionListIndex, it.payType.rules), ITAFieldAttr(timeWithFormat(it.getMinutes(), decimalFormat, decimalPlaces = 2),
                        it.minutes.security, it.minutes.optionListIndex, it.minutes.rules), it.effectiveDate, it.shiftType, it.scheduleDeviationId, it.allDayFlag, it.startDate, it.stopDate, it.deviationCode, it.orgLevels)
            }
            .toList()
            .map { getHoursColumnConfig(it, hoursList.referenceData) }
            .subscribe { emitStickyChange(OnTimeEntryHoursReceived(it.first, it.second)) }

    private fun getHoursColumnConfig(items: MutableList<HoursList.Hours>, referenceData: HoursList.ReferenceData): Pair<List<TimeEntryColumnData>, List<TimeEntryRowData>> {
        val columnNames = intArrayOf(R.string.eff_date_header_text, R.string.pay_type_header_text, R.string.hours_header_text)
        val columnValuesFns: Array<(Any) -> String> = arrayOf({ i -> (i as HoursList.Hours).effectiveDate.sv }, { i -> (i as HoursList.Hours).payType.sv }, { i -> (i as HoursList.Hours).minutes.value })
        val itemDetail: (Any) -> TimeEntryDetailItem = { TimeEntryDetailItem(context.getString(R.string.detail_hours_header_text), getHoursDetails(it as HoursList.Hours, referenceData), "Hours") }
        return generateTableData(columnNames, columnValuesFns, itemDetail, items, rightAlignedFn = { it == R.string.hours_header_text })
    }

    private fun getHoursDetails(hour: HoursList.Hours, referenceData: HoursList.ReferenceData): List<AdapterItem> {
        val items = ArrayList<AdapterItem>()
        if (hour.actualDate.security != 0) items.add(TimeEntryItem(context.getString(R.string.actual_date_header_text), hour.actualDate.value))
        if (hour.effectiveDate.security != 0) items.add(TimeEntryItem(context.getString(R.string.effective_date_header_text), hour.effectiveDate.value))
        if (hour.payType.security != 0) items.add(TimeEntryItem(context.getString(R.string.pay_type_header_text), hour.payType.value))
        if (hour.minutes.security != 0) items.add(TimeEntryItem(context.getString(R.string.hours_header_text), hour.minutes.value))
        if (hour.shiftType.security != 0) items.add(TimeEntryItem(context.getString(R.string.shift_type_header_text), referenceData.optionLists.shiftType[hour.shiftType.optionListIndex ?: 0]
                .firstOrNull { it.value == hour.shiftType.value }?.label ?: ""))
        if (hour.deviceNum.security != 0) items.add(TimeEntryItem(context.getString(R.string.device_header_text), hour.deviceNum.value))
        if (hour.comment.security != 0) items.add(TimeEntryItem(context.getString(R.string.comment_header_text), if (hour.comment.value.isNotEmpty()) hour.comment.value else context.getString(R.string.punch_no_comment_text)))
        var firstTime = true
        for (orgLevelEntry in hour.orgLevels) {
            if (orgLevelEntry.value.security != 0) {
                if (firstTime) {
                    items.add(TimeEntrySeparator())
                    items.add(TimeEntrySection(context.getString(R.string.organization_levels_hint_text)))
                    firstTime = false
                }
                items.add(TimeEntryItem(referenceData.orgLevels[orgLevelEntry.key]?.name ?: "", referenceData.orgLevels[orgLevelEntry.key]?.items?.let { it[orgLevelEntry.value.optionListIndex ?: 0] }
                        ?.first { it.value == orgLevelEntry.value.value }?.label ?: ""))
            }
        }
        if (hour.scheduleDeviationId.value == "0") return items
        if (hour.allDayFlag.security != 0 || hour.startDate.security != 0 || hour.stopDate.security != 0 || hour.deviationCode.security != 0) {
            items.add(TimeEntrySeparator())
            items.add(TimeEntrySection(context.getString(R.string.schedule_deviation_hint_text)))
        }
        if (hour.allDayFlag.security != 0) items.add(TimeEntryItem(context.getString(R.string.scheduling_header_text), referenceData.optionLists.allDayFlag[hour.allDayFlag.optionListIndex ?: 0]
                .firstOrNull { it.value == hour.allDayFlag.value }?.label ?: ""))
        if (hour.startDate.security != 0) items.add(TimeEntryItem(context.getString(R.string.start_date_header_text), hour.startDate.value))
        if (hour.stopDate.security != 0) items.add(TimeEntryItem(context.getString(R.string.stop_date_header_text), hour.stopDate.value))
        if (hour.deviationCode.security != 0) items.add(TimeEntryItem(context.getString(R.string.deviation_type_header_text), referenceData.optionLists.deviationCode?.let { it[hour.deviationCode.optionListIndex ?: 0] }
                ?.firstOrNull { it.value == hour.deviationCode.value }?.label ?: ""))
        return items
    }

    //Punches section
    private fun processPunches(punchList: PunchList) = Observable.from(punchList.content.punch)
            .filter { it.punchCategory.optionListIndex != null }
            .map {
                val punchCategory = punchList.referenceData.optionLists.punchCategory[it.punchCategory.optionListIndex ?: 0].firstOrNull { category -> category.value == it.punchCategory.value }?.label
                return@map PunchList.Punch(it.punchId, it.actualTimedate, it.comment, ITAFieldAttr(punchCategory ?: "", it.punchCategory.security, it.punchCategory.optionListIndex), it.roundedTimedate, it.roundSource, it.payType, it.shiftType,
                        it.deviceNum, it.sourceCode, it.employeeAttendanceRecords, it.errorNumber, it.orgLevels)
            }
            .toList()
            .map { getPunchesColumnConfig(it, punchList.referenceData) }
            .subscribe { emitStickyChange(OnTimeEntryPunchesReceived(it.first, it.second)) }

    private fun getPunchesColumnConfig(items: MutableList<PunchList.Punch>, referenceData: PunchList.ReferenceData): Pair<List<TimeEntryColumnData>, List<TimeEntryRowData>> {
        val columnNames = intArrayOf(R.string.actual_time_date_header_text, R.string.rounded_time_date_header_text, R.string.punch_type_header_text, R.string.info_header_text)
        val columnValuesFns: Array<(Any) -> String> = arrayOf({ i -> (i as PunchList.Punch).actualTimedate.sv }, { i -> (i as PunchList.Punch).roundedTimedate.sv }, { i -> (i as PunchList.Punch).punchCategory.sv }, { i -> (i as PunchList.Punch).getInfoField(context) })
        val highlightedFn: (Any) -> Boolean = { i -> (i as PunchList.Punch).errorNumber?.value == "M" }
        val itemDetail: (Any) -> TimeEntryDetailItem = { TimeEntryDetailItem(context.getString(R.string.detail_punches_header_text), getPunchesDetails(it as PunchList.Punch, referenceData), "Punch") }
        return generateTableData(columnNames, columnValuesFns, itemDetail, items, highlightedFn)
    }

    private fun getPunchesDetails(punch: PunchList.Punch, referenceData: PunchList.ReferenceData): List<AdapterItem> {
        val items = ArrayList<AdapterItem>()
        if (punch.actualTimedate.security != 0) items.add(TimeEntryItem(context.getString(R.string.actual_time_date_header_text), punch.actualTimedate.value))
        if (punch.punchCategory.security != 0) items.add(TimeEntryItem(context.getString(R.string.punch_category_header_text), punch.punchCategory.value))
        if (punch.roundedTimedate.security != 0) items.add(TimeEntryItem(context.getString(R.string.rounded_time_date_header_text), punch.roundedTimedate.value))
        if (punch.roundSource.security != 0) items.add(TimeEntryItem(context.getString(R.string.round_source_header_text), referenceData.optionLists.roundSource?.let { it[punch.roundSource.optionListIndex ?: 0] }
                ?.firstOrNull { it.value == punch.roundSource.value }?.label ?: ""))
        if (punch.payType.security != 0) items.add(TimeEntryItem(context.getString(R.string.pay_type_header_text), referenceData.optionLists.payType?.let { it[punch.payType.optionListIndex ?: 0] }
                ?.firstOrNull { it.value == punch.payType.value }?.label ?: ""))
        if (punch.shiftType.security != 0) items.add(TimeEntryItem(context.getString(R.string.shift_type_header_text), referenceData.optionLists.shiftType?.let { it[punch.shiftType.optionListIndex ?: 0] }
                ?.firstOrNull { it.value == punch.shiftType.value }?.label ?: ""))
        if (punch.deviceNum.security != 0) items.add(TimeEntryItem(context.getString(R.string.device_header_text), referenceData.optionLists.deviceNum?.let { it[punch.deviceNum.optionListIndex ?: 0] }
                ?.firstOrNull { it.value == punch.deviceNum.value }?.label ?: ""))
        if (punch.comment.security != 0) items.add(TimeEntryItem(context.getString(R.string.comment_header_text), if (punch.comment.value.isNotEmpty()) punch.comment.value else context.getString(R.string.punch_no_comment_text)))
        var firstTime = true
        for (orgLevelEntry in punch.orgLevels) {
            if (orgLevelEntry.value.security != 0) {
                if (firstTime) {
                    items.add(TimeEntrySeparator())
                    items.add(TimeEntrySection(context.getString(R.string.organization_levels_hint_text)))
                    firstTime = false
                }
                items.add(TimeEntryItem(referenceData.orgLevels[orgLevelEntry.key]?.name ?: "", referenceData.orgLevels[orgLevelEntry.key]?.items?.let { it[orgLevelEntry.value.optionListIndex ?: 0] }
                        ?.first { it.value == orgLevelEntry.value.value }?.label ?: ""))
            }
        }
        return items
    }

    //Dollars section
    private fun processDollars(dollarList: DollarList) {
        Observable.from(dollarList.content.dollar)
                .map {
                    val payType = dollarList.referenceData.optionLists.payType[it.payType.optionListIndex ?: 0].firstOrNull { payType -> payType.value == it.payType.value }
                    DollarList.Dollar(it.effectiveDate, it.coverageStartDate, it.coverageStopDate, it.amount, ITAFieldAttr(payType?.label ?: "", it.payType.security, it.payType.optionListIndex, it.payType.rules), it.comment, it.deviceNum,
                            it.sourceCode, it.orgLevels, it.mileageId, it.beginMiles, it.endMiles, it.totalMiles, it.ratePerMile, it.vehicleNumber)
                }
                .toList()
                .map { getDollarsColumnConfig(it, dollarList.referenceData) }
                .subscribe { emitStickyChange(OnTimeEntryDollarsReceived(it.first, it.second)) }
    }

    private fun getDollarsColumnConfig(items: MutableList<DollarList.Dollar>, referenceData: DollarList.ReferenceData): Pair<List<TimeEntryColumnData>, List<TimeEntryRowData>> {
        val columnNames = intArrayOf(R.string.eff_date_header_text, R.string.dollars_header_text, R.string.pay_type_header_text)
        val columnValuesFns: Array<(Any) -> String> = arrayOf({ i -> (i as DollarList.Dollar).effectiveDate.sv }, { i -> (i as DollarList.Dollar).amount.sv }, { i -> (i as DollarList.Dollar).payType.sv })
        val itemDetail: (Any) -> TimeEntryDetailItem = { TimeEntryDetailItem(context.getString(if ((it as DollarList.Dollar).mileageId.value.toIntOrNull() != 0) R.string.detail_mileage_header_text else R.string.detail_dollars_header_text), getDollarDetails(it, referenceData), "Dollars") }
        return generateTableData(columnNames, columnValuesFns, itemDetail, items, rightAlignedFn = { it == R.string.dollars_header_text })
    }

    private fun getDollarDetails(dollar: DollarList.Dollar, referenceData: DollarList.ReferenceData): List<AdapterItem> {
        val isMileage = dollar.mileageId.value.toIntOrNull() != 0
        val items = ArrayList<AdapterItem>()
        if (dollar.effectiveDate.security != 0) items.add(TimeEntryItem(context.getString(R.string.effective_date_header_text), dollar.effectiveDate.value))
        if (dollar.payType.security != 0) items.add(TimeEntryItem(context.getString(R.string.pay_type_header_text), dollar.payType.value))
        if (dollar.amount.security != 0) items.add(TimeEntryItem(context.getString(R.string.amount_header_text), dollar.amount.value))
        if (!isMileage && dollar.coverageStartDate.security != 0) items.add(TimeEntryItem(context.getString(R.string.start_date_header_text), dollar.coverageStartDate.value))
        if (!isMileage && dollar.coverageStopDate.security != 0) items.add(TimeEntryItem(context.getString(R.string.end_date_header_text), dollar.coverageStopDate.value))
        if (!isMileage && dollar.deviceNum.security != 0) items.add(TimeEntryItem(context.getString(R.string.device_header_text), referenceData.optionLists.deviceNum[dollar.deviceNum.optionListIndex ?: 0].firstOrNull { it.value == dollar.deviceNum.value }?.label ?: ""))
        if (isMileage && (dollar.vehicleNumber?.security ?: 0) != 0) items.add(TimeEntryItem(context.getString(R.string.vehicle_header_text), referenceData.optionLists.vehicleNumber[dollar.vehicleNumber?.optionListIndex ?: 0].firstOrNull { it.value == dollar.vehicleNumber?.value }?.label ?: ""))
        if (isMileage && (dollar.ratePerMile?.security ?: 0) != 0) items.add(TimeEntryItem(context.getString(R.string.rate_header_text), dollar.ratePerMile?.value ?: ""))
        if (isMileage && (dollar.beginMiles?.security ?: 0) != 0) items.add(TimeEntryItem(context.getString(R.string.begin_miles_header_text), dollar.beginMiles?.value ?: ""))
        if (isMileage && (dollar.endMiles?.security ?: 0) != 0) items.add(TimeEntryItem(context.getString(R.string.end_miles_header_text), dollar.endMiles?.value ?: ""))
        if (isMileage && (dollar.totalMiles?.security ?: 0) != 0) items.add(TimeEntryItem(context.getString(R.string.total_miles_header_text), dollar.totalMiles?.value ?: ""))
        if (dollar.comment.security != 0) items.add(TimeEntryItem(context.getString(R.string.comment_header_text), if (dollar.comment.value.isNotEmpty()) dollar.comment.value else context.getString(R.string.punch_no_comment_text)))

        var firstTime = true
        for (orgLevelEntry in dollar.orgLevels) {
            if (orgLevelEntry.value.security != 0) {
                if (firstTime) {
                    items.add(TimeEntrySeparator())
                    items.add(TimeEntrySection(context.getString(R.string.organization_levels_hint_text)))
                    firstTime = false
                }
                items.add(TimeEntryItem(referenceData.orgLevels[orgLevelEntry.key]?.name ?: "", referenceData.orgLevels[orgLevelEntry.key]?.items?.let { it[orgLevelEntry.value.optionListIndex ?: 0] }
                        ?.first { it.value == orgLevelEntry.value.value }?.label ?: ""))
            }
        }
        return items
    }

    private fun generateTableData(columnNames: IntArray, columnValuesFns: Array<(Any) -> String>, itemDetail: ((Any) -> TimeEntryDetailItem)? = null, items: MutableList<*>, highlightedFn: (Any) -> Boolean = { false }, rightAlignedFn: (Int) -> Boolean = { false }): Pair<List<TimeEntryColumnData>, List<TimeEntryRowData>> {
        val properties = ArrayList<TimeEntryColumnData>()
        val adapterItems = ArrayList<TimeEntryRowData>()
        var itemDataList = ArrayList<String>()
        var cellData: String?
        var textWidth: Int
        var columnHeader: String
        for (name in columnNames) {
            columnHeader = context.getString(name)
            properties.add(TimeEntryColumnData(columnHeader, appUtil.getTextWidth(columnHeader), rightAlignedFn(name), emptyColumn = name != R.string.hours_header_text && name != R.string.dollars_header_text))
        }
        for (item in items) {
            if (item == null) continue
            for (index in 0 until columnValuesFns.size) {
                cellData = columnValuesFns[index](item)
                textWidth = appUtil.getTextWidth(cellData)
                if (!cellData.isNullOrEmpty() && cellData != "0h" && cellData.toDoubleOrNull() != 0.0) properties[index].emptyColumn = false
                properties[index].width = Math.max(properties[index].width, textWidth)
                itemDataList.add(cellData)
            }

            adapterItems.add(TimeEntryRowData(itemDataList, highlightedFn(item), if (itemDetail == null) null else itemDetail(item)))
            itemDataList = ArrayList()
        }
        var index = 0
        while (index < properties.size) {
            if (properties[index].emptyColumn) {
                properties.removeAt(index)
                for (item in adapterItems) {
                    item.data.removeAt(index)
                }
                continue
            }
            ++index
        }
        return Pair(properties, adapterItems)
    }

    private fun processPayGroupsWithEmployeeLists(employeeList: EmployeeList) = Observable.from(employeeList.payGroups)
            .concatMap { pg ->
                Observable.zip(Observable.from(employeeList.employeeList).filter { pg.payGroupId == it.payGroupId }.toSortedList { l, r -> l.name.compareTo(r.name) }.filter { it.isNotEmpty() },
                        Observable.from(employeeList.payPeriods).takeFirst { pg.payPeriodId == it.payPeriodId })
                { f, s -> TimeEntryPayGroup(pg.payGroupId, pg.name, s.currentPeriod.idx, s.currentPeriod.startDate, s.currentPeriod.stopDate, f) }
            }
            .toList()
            .subscribe { emitChange(OnPayGroupsAndEmployeesReceived(it)) }

    private fun processLoadedEmployees(employeeData: EmployeeBatch, loadedEmployees: List<EmployeeList.Employee>, loadingPayGroupId: Int) = Observable.from(loadedEmployees)
            .concatMap { employee ->
                Observable.from(employeeData.content.approvalStatus)
                        .takeFirst { employee.employeeId == it.employeeId }
                        .map {
                            TimeEntryEmployee(employee.employeeId, employee.name, employee.employeeNumber, it.data?.approvalStatus?.statusFlags ?: 0,
                                    timeWithFormat(it.data?.minutes ?: 0, decimalFormatted = employeeData.referenceData.timeFormat == HOURS_FLOAT, decimalPlaces = 2), it.data?.dollars ?: "0.00")
                        }
            }
            .toList()
            .subscribe { onEmployeeBatchProcessedSubject.onNext(OnEmployeeBatchProcessed(it, loadingPayGroupId, approving = false)) }

    private fun processLoadedEmployee(employeeData: List<EmployeeBatch.ApprovalStatus>, employee: TimeEntryEmployee, error: String, loadingPayGroupId: Int) = Observable.from(employeeData)
            .takeFirst { it.employeeId == employee.employeeId }
            .map { TimeEntryEmployee(it.employeeId, employee.employeeName, employee.employeeNumber, it.data?.approvalStatus?.statusFlags ?: 0, employee.hours, employee.dollars, if (error.isEmpty()) null else error) }
            .toList()
            .subscribe { onEmployeeBatchProcessedSubject.onNext(OnEmployeeBatchProcessed(it, loadingPayGroupId, approving = true)) }

    private fun processMassApproval(approvalResponse: Response<TimeApprovalStatus>, statusListResponse: List<EmployeeBatch.ApprovalStatus>, selectedEmployees: List<TimeEntryEmployee>, loadingPayGroupId: Int) = Observable.from(statusListResponse)
            .concatMap { status ->
                Observable.from(selectedEmployees)
                        .takeFirst { it.employeeId == status.employeeId }
                        .concatMap { employee ->
                            Observable.from(approvalResponse.data.content.results)
                                    .takeFirst { it.employeeId == employee.employeeId }
                                    .map { TimeEntryEmployee(it.employeeId, employee.employeeName, employee.employeeNumber, status.data?.approvalStatus?.statusFlags ?: 0, employee.hours, employee.dollars, it.error?.message) }
                        }
            }
            .toList()
            .subscribe { onEmployeeBatchProcessedSubject.onNext(OnEmployeeBatchProcessed(it, loadingPayGroupId, approving = true)) }
}