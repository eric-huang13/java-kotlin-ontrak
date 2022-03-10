package com.insperity.escmobile.store

import com.crashlytics.android.Crashlytics
import com.insperity.escmobile.action.Action
import com.insperity.escmobile.action.Keys
import com.insperity.escmobile.dispatcher.Dispatcher
import org.greenrobot.eventbus.EventBus
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.Subject
import timber.log.Timber
import java.net.UnknownHostException

abstract class MainStore protected constructor(dispatcher: Dispatcher, private val bus: EventBus) {

    init {
        dispatcher.actionsHub
                .doOnError { this.logError(it) }
                .subscribeOn(Schedulers.computation())
                .subscribe { this.onActionReceived(it) }
    }

    protected abstract fun onActionReceived(action: Action)

    protected fun logError(action: Action) = logError(action.getByKey(Keys.ERROR) as Throwable)

    protected fun logError(throwable: Throwable) {
        Timber.e(throwable, throwable.message)
        if (throwable is UnknownHostException) {
            return
        }
        Crashlytics.logException(throwable)
    }

    protected fun emitChange(event: Any) = emitChange { bus.post(event) }

    protected fun emitStickyChange(event: Any) = emitChange { bus.postSticky(event) }

    private fun emitChange(action: () -> Unit) {
        Subject.fromCallable(action)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    protected fun logOnActionNotCaught(actionType: String) = Timber.d("Action type %s was not caught by %s", actionType, this.javaClass.simpleName)
}
