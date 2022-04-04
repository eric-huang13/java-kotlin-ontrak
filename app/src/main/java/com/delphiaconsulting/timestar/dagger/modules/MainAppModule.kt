package com.delphiaconsulting.timestar.dagger.modules

import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.delphiaconsulting.timestar.BuildConfig
import com.delphiaconsulting.timestar.action.Action
import com.delphiaconsulting.timestar.data.DaoMaster
import com.delphiaconsulting.timestar.data.DaoSession
import com.delphiaconsulting.timestar.data.DatabaseHelper
import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.net.analytics.Tracker
import com.delphiaconsulting.timestar.net.analytics.impl.TrackerImpl
import com.delphiaconsulting.timestar.util.ConnectionUtil
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