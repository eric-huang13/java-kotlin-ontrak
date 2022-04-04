package com.delphiaconsulting.timestar.action.creators

import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.net.service.TimeEntryService
import com.delphiaconsulting.timestar.util.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by dxsier on 2/26/18.
 */

@Singleton
class TimeEntryActionsCreator @Inject constructor(dispatcher: Dispatcher, timeEntryService: TimeEntryService, preferences: Preferences) : MainTimeEntryActionsCreator(dispatcher, null, timeEntryService, preferences)
