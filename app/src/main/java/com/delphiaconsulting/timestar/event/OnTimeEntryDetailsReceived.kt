package com.delphiaconsulting.timestar.event

import com.delphiaconsulting.timestar.view.common.TimeEntryColumnData
import com.delphiaconsulting.timestar.view.common.TimeEntryRowData

class OnTimeEntryDetailsReceived(val properties: List<TimeEntryColumnData>, val items: List<TimeEntryRowData>)