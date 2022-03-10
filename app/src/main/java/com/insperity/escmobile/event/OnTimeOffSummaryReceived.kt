package com.insperity.escmobile.event

import com.insperity.escmobile.net.gson.TimeOffSummary

class OnTimeOffSummaryReceived(val accrualBalances: List<TimeOffSummary.AccrualBalance>)