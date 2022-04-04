package com.delphiaconsulting.timestar.event

import com.delphiaconsulting.timestar.util.TimeOffStatuses

class OnTimeOffRequestClicked(val requestId: String, val position: Int, val status: TimeOffStatuses?)
