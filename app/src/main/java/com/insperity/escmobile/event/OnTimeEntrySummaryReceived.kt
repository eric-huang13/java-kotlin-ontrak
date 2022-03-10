package com.insperity.escmobile.event

import com.insperity.escmobile.view.common.TimeEntryColumnData
import com.insperity.escmobile.view.common.TimeEntryRowData

/**
 * Created by dxsier on 2/26/18.
 */
class OnTimeEntrySummaryReceived(val properties: List<TimeEntryColumnData>, val items: List<TimeEntryRowData>)