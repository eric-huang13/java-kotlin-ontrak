package com.delphiaconsulting.timestar.event

import com.delphiaconsulting.timestar.net.gson.TimeOffApprovalRequests

/**
 * Created by dxsier on 7/6/17.
 */

class OnTimeOffApprovalRequestsReceived(val unanswered: List<TimeOffApprovalRequests.TimeOffApprovalRequest>, val approved: List<TimeOffApprovalRequests.TimeOffApprovalRequest>, val declined: List<TimeOffApprovalRequests.TimeOffApprovalRequest>)
