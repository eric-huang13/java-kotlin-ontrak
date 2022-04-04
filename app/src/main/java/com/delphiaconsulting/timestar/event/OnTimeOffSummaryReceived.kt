package com.delphiaconsulting.timestar.event

import com.delphiaconsulting.timestar.net.gson.TimeOffSummary

class OnTimeOffSummaryReceived(val accrualBalances: List<TimeOffSummary.AccrualBalance>)