package com.insperity.escmobile.event

import com.insperity.escmobile.view.common.TimeEntryColumnData
import com.insperity.escmobile.view.common.TimeEntryRowData

class OnTimeEntryPunchesReceived(val properties: List<TimeEntryColumnData>, val items: List<TimeEntryRowData>)