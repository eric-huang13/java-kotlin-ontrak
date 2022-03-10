package com.insperity.escmobile.event

import com.insperity.escmobile.net.gson.TimeOffApprovalRequests

/**
 * Created by dxsier on 7/6/17.
 */

class OnTimeOffApprovalRequestsReceived(val unanswered: List<TimeOffApprovalRequests.TimeOffApprovalRequest>, val approved: List<TimeOffApprovalRequests.TimeOffApprovalRequest>, val declined: List<TimeOffApprovalRequests.TimeOffApprovalRequest>)
