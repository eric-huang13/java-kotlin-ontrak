package com.delphiaconsulting.timestar.action.creators

import com.delphiaconsulting.timestar.dispatcher.Dispatcher

abstract class ActionsCreator(dispatcher: Dispatcher, taskCache: Any?) : MainActionsCreator(dispatcher)
