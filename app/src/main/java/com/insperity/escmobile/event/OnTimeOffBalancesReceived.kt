package com.insperity.escmobile.event

import com.insperity.escmobile.net.gson.TimeOffBalances
import com.insperity.escmobile.net.gson.TimeOffBalancesMeta
import com.insperity.escmobile.net.gson.TimeOffRequestsMeta

class OnTimeOffBalancesReceived(val timeOffBalances: TimeOffBalances, val timeOffBalancesMeta: TimeOffBalancesMeta, val timeOffRequestsMeta: TimeOffRequestsMeta)
