package com.delphiaconsulting.timestar.event

import com.delphiaconsulting.timestar.view.common.TimeEntryColumnData
import com.delphiaconsulting.timestar.view.common.TimeEntryRowData

/**
 * Created by dxsier on 2/26/18.
 */
class OnTimeEntrySummaryReceived(val properties: List<TimeEntryColumnData>, val items: List<TimeEntryRowData>)