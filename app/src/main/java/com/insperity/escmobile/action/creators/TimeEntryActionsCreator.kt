package com.insperity.escmobile.action.creators

import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.net.service.TimeEntryService
import com.insperity.escmobile.util.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by dxsier on 2/26/18.
 */

@Singleton
class TimeEntryActionsCreator @Inject constructor(dispatcher: Dispatcher, timeEntryService: TimeEntryService, preferences: Preferences) : MainTimeEntryActionsCreator(dispatcher, null, timeEntryService, preferences)
