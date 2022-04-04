package com.delphiaconsulting.timestar.store

import com.delphiaconsulting.timestar.action.Action
import com.delphiaconsulting.timestar.action.Keys
import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.event.OnUnauthorizedAccess

import org.greenrobot.eventbus.EventBus

import java.net.HttpURLConnection

import retrofit2.adapter.rxjava.HttpException

abstract class Store protected constructor(dispatcher: Dispatcher, bus: EventBus) : MainStore(dispatcher, bus) {

    protected fun handleCommonError(action: Action): Boolean {
        val throwable = action.getByKey(Keys.ERROR) as Throwable
        val handled = handleHttpException(throwable)
        if (!handled) {
            logError(throwable)
        }
        return handled
    }

    private fun handleHttpException(throwable: Throwable): Boolean {
        if (throwable is HttpException && throwable.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            emitChange(OnUnauthorizedAccess())
            return true
        }
        return false
    }
}
