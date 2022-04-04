package com.delphiaconsulting.timestar.event

import com.delphiaconsulting.timestar.view.common.TimeEntryColumnData
import com.delphiaconsulting.timestar.view.common.TimeEntryRowData

class OnTimeEntryPunchesReceived(val properties: List<TimeEntryColumnData>, val items: List<TimeEntryRowData>)