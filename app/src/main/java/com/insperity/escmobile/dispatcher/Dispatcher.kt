package com.insperity.escmobile.dispatcher

import com.insperity.escmobile.action.Action
import rx.subjects.PublishSubject
import javax.inject.Singleton

@Singleton
class Dispatcher(val actionsHub: PublishSubject<Action>) {

    fun dispatch(action: String, vararg data: Any) {
        if (action.isEmpty()) {
            actionsHub.onError(IllegalArgumentException("Type must not be empty"))
            return
        }

        if (data.size % 2 != 0) {
            actionsHub.onError(IllegalArgumentException("Data must be a valid list of key, value pairs"))
            return
        }

        val actionBuilder = Action.type(action)
        var i = 0
        while (i < data.size) {
            val key = data[i++] as Int
            val value = data[i++]
            actionBuilder.bundle(key, value)
        }
        actionsHub.onNext(actionBuilder.build())
    }
}
