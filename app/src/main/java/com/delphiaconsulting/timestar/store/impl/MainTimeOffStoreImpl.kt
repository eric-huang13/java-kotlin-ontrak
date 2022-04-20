package com.delphiaconsulting.timestar.store.impl

import android.annotation.SuppressLint
import android.util.Pair
import android.util.SparseArray
import com.delphiaconsulting.timestar.action.Action
import com.delphiaconsulting.timestar.action.Actions
import com.delphiaconsulting.timestar.action.Keys
import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.event.*
import com.delphiaconsulting.timestar.net.gson.*
import com.delphiaconsulting.timestar.store.Store
import com.delphiaconsulting.timestar.store.TimeOffStore
import com.delphiaconsulting.timestar.util.TimeOffStatuses
import org.greenrobot.eventbus.EventBus
import rx.Observable
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by qktran on 1/23/17.
 */

open class MainTimeOffStoreImpl(dispatcher: Dispatcher, bus: EventBus) : Store(dispatcher, bus), TimeOffStore {

    //@Suppress("UNCHECKED_CAST")
    override fun onActionReceived(action: Action) {
        when (action.type) {
            Actions.TIME_OFF_SUMMARY_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_OFF_BALANCES) as Response<TimeOffSummary>
                if (response.message != null) {
                    handleCommonError(action)
                    return
                }
                emitChange(OnTimeOffSummaryReceived(response.data.content.accrualBalance))
            }
            Actions.TIME_OFF_REQUESTS_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_OFF_REQUESTS) as Response<TimeOffRequests>
                emitChange(OnTimeOffRequestsReceived(response.data))
            }
            Actions.TIME_OFF_REQUEST_DETAILS_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_OFF_REQUEST_DETAILS) as Response<TimeOffRequestDetails>
                if (response.message != null) {
                    emitChange(OnTimeOffRequestDetailsError(response.message.message))
                    return
                }
                emitStickyChange(OnTimeOffRequestDetailsReceived(response.data))
            }
            Actions.TIME_OFF_CREATION_DATA_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_OFF_BALANCES) as Triple<Response<TimeOffBalances>, Response<TimeOffBalancesMeta>, Response<TimeOffRequestsMeta>>
                emitStickyChange(OnTimeOffBalancesReceived(response.first.data, response.second.data, response.third.data))
            }
            Actions.TIME_OFF_REQUEST_SUBMITTED -> {
                val response = action.getByKey(Keys.TIME_OFF_SUBMISSION_RESULT) as Response<TimeOffSubmitResponse>
                if (response.message != null) {
                    emitChange(OnTimeOffRequestSubmissionError(response.message.message))
                    return
                }
                emitStickyChange(OnTimeOffRequestSubmitted())
            }
            Actions.TIME_OFF_DATES_BALANCES_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_OFF_BALANCES) as Response<TimeOffBalances>
                getDateBalancesCalculation(response.data.periodBalances)
            }
            Actions.TIME_OFF_REQUESTS_REVIEW_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_OFF_REQUESTS) as Response<TimeOffApprovalRequests>
                processApprovalRequests(response.data)
            }
            Actions.TIME_OFF_RESOLUTION_REQUEST_RECEIVED -> {
                val response = action.getByKey(Keys.TIME_OFF_RESOLUTION_RESULT) as Response<TimeOffResolutionResult>
                if (response.message != null) {
                    emitChange(OnTimeOffResolutionSubmissionError(response.message.message))
                    return
                }
                emitChange(OnTimeOffResolutionSubmitted())
            }
            Actions.TIME_OFF_RESOLUTION_REQUEST_ERROR -> if (!handleCommonError(action)) {
                emitChange(OnTimeOffResolutionSubmissionError())
            }
            Actions.TIME_OFF_DATES_BALANCES_ERROR -> {
                emitChange(OnDateBalancesServiceError())
                handleCommonError(action)
            }
            Actions.TIME_OFF_REQUEST_SUBMISSION_ERROR -> {
                emitChange(OnTimeOffRequestSubmissionError())
                handleCommonError(action)
            }
            Actions.TIME_OFF_SUMMARY_ERROR, Actions.TIME_OFF_CREATION_DATA_ERROR, Actions.TIME_OFF_REQUESTS_ERROR, Actions.TIME_OFF_REQUESTS_REVIEW_ERROR, Actions.TIME_OFF_REQUEST_DETAILS_ERROR -> handleCommonError(action)
            else -> logOnActionNotCaught(action.type)
        }
    }

    @SuppressLint("UseSparseArrays")
    private fun getDateBalancesCalculation(balances: List<TimeOffBalances.TimeOffBalance>) {
        val balanceDates = SparseArray<MutableMap<String, Double>>()
        val balanceDatesGroup = TreeMap<Int, ArrayList<TimeOffBalances.BalanceDate>>()
        Observable.from(balances)
                .doOnNext {
                    if (balanceDatesGroup[it.accrualId] == null) {
                        balanceDatesGroup[it.accrualId] = ArrayList()
                    }
                    balanceDatesGroup[it.accrualId]?.addAll(it.dates)
                }.toList()
                .concatMap { Observable.from(balanceDatesGroup.entries) }
                .concatMap { dateEntry -> Observable.from(dateEntry.value).toList().map { Pair(dateEntry.key, it) } }
                .concatMap { pair ->
                    balanceDates.put(pair.first, TreeMap())
                    Observable.from(pair.second)
                            .toSortedList { left, right -> left.compareTo(right) }
                            .concatMapIterable { it }
                            .doOnNext { balanceDates[pair.first].put(it.date, it.balance) }
                }
                .doOnCompleted { emitChange(OnNewDateBalancesAvailable(balanceDates)) }
                .subscribe()
    }

    private fun processApprovalRequests(requests: TimeOffApprovalRequests) {
        val unanswered = ArrayList<TimeOffApprovalRequests.TimeOffApprovalRequest>()
        val approved = ArrayList<TimeOffApprovalRequests.TimeOffApprovalRequest>()
        val declined = ArrayList<TimeOffApprovalRequests.TimeOffApprovalRequest>()
        for (request in requests.content.request) {
            if (request.computedRequestStatus == TimeOffStatuses.CANCELLED || request.computedRequestStatus == TimeOffStatuses.WITHDRAWN || request.computedRecipientStatus == TimeOffStatuses.NO_ACTION) {
                continue
            }
            if (request.computedRecipientStatus == TimeOffStatuses.DECLINED) {
                declined.add(request)
                continue
            }
            if (request.computedRecipientStatus == TimeOffStatuses.APPROVED) {
                approved.add(request)
                continue
            }
            unanswered.add(request)
        }
        emitStickyChange(OnTimeOffApprovalRequestsReceived(unanswered, approved, declined))
    }
}
