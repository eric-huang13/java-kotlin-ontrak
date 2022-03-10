package com.insperity.escmobile.dagger.modules

import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.insperity.escmobile.BuildConfig
import com.insperity.escmobile.action.Action
import com.insperity.escmobile.data.DaoMaster
import com.insperity.escmobile.data.DaoSession
import com.insperity.escmobile.data.DatabaseHelper
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.net.analytics.Tracker
import com.insperity.escmobile.net.analytics.impl.TrackerImpl
import com.insperity.escmobile.util.ConnectionUtil
import dagger.Module
import dagger.Provides
import org.greenrobot.eventbus.EventBus
import rx.subjects.PublishSubject
import javax.inject.Named
import javax.inject.Singleton

@Module
class MainAppModule {

    @Provides
    @Singleton internal fun provideEventBus(): EventBus = EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).build()

    @Provides
    @Singleton internal fun providePublishSubject(): PublishSubject<Action> = PublishSubject.create()

    @Provides
    @Singleton internal fun provideDispatcher(actionsHub: PublishSubject<Action>): Dispatcher = Dispatcher(actionsHub)

    @Provides
    @Singleton internal fun provideConnectionUtil(context: Context): ConnectionUtil = ConnectionUtil(context)

    @Provides
    @Singleton internal fun provideTracker(context: Context): Tracker = TrackerImpl(context)

    @Provides
    @Singleton internal fun provideInputMethodManager(context: Context): InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    @Provides
    @Singleton
    @Named("main_dao_session") internal fun provideMainDaoSession(context: Context): DaoSession {
        val db = DatabaseHelper(context, "main_data.db").writableDb
        return DaoMaster(db).newSession()
    }
}