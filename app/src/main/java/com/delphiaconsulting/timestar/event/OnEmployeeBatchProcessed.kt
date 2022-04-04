package com.delphiaconsulting.timestar.event

import com.delphiaconsulting.timestar.view.common.TimeEntryEmployee

class OnEmployeeBatchProcessed(val employeeBatch: List<TimeEntryEmployee>, val loadingPayGroupId: Int, val approving: Boolean)