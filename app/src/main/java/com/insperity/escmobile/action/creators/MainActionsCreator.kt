package com.insperity.escmobile.action.creators

import com.insperity.escmobile.dispatcher.Dispatcher
import rx.Observable
import rx.Subscription
import rx.schedulers.Schedulers
import rx.subjects.Subject

abstract class MainActionsCreator(protected var dispatcher: Dispatcher) {

    protected open fun executeTask(observable: Observable<*>, success: (Any) -> Unit, error: (Throwable) -> Unit): Subscription = observable.subscribeOn(Schedulers.io()).subscribe(success, error)

    protected fun executeTask(action: () -> Unit): Subscription = Subject.fromCallable(action).subscribeOn(Schedulers.io()).subscribe()
}
