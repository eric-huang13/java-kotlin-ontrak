package com.insperity.escmobile.event

import com.insperity.escmobile.view.common.TimeEntryEmployee

class OnEmployeeBatchProcessed(val employeeBatch: List<TimeEntryEmployee>, val loadingPayGroupId: Int, val approving: Boolean)