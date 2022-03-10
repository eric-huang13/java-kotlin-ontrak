package com.insperity.escmobile.store

import com.insperity.escmobile.action.Action
import com.insperity.escmobile.action.Keys
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.event.OnUnauthorizedAccess

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
