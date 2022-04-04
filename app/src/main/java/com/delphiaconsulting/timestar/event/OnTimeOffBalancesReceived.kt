package com.delphiaconsulting.timestar.event

import com.delphiaconsulting.timestar.net.gson.TimeOffBalances
import com.delphiaconsulting.timestar.net.gson.TimeOffBalancesMeta
import com.delphiaconsulting.timestar.net.gson.TimeOffRequestsMeta

class OnTimeOffBalancesReceived(val timeOffBalances: TimeOffBalances, val timeOffBalancesMeta: TimeOffBalancesMeta, val timeOffRequestsMeta: TimeOffRequestsMeta)
