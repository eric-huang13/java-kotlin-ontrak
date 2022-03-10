package com.insperity.escmobile.action.creators

import com.insperity.escmobile.dispatcher.Dispatcher

abstract class ActionsCreator(dispatcher: Dispatcher, taskCache: Any?) : MainActionsCreator(dispatcher)
