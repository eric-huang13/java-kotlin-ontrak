package com.delphiaconsulting.timestar.dagger.modules

import android.content.Context
import com.delphiaconsulting.timestar.data.DaoSession
import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.net.analytics.Tracker
import com.delphiaconsulting.timestar.store.*
import com.delphiaconsulting.timestar.store.impl.*
import com.delphiaconsulting.timestar.util.Preferences
import dagger.Module
import dagger.Provides
import org.greenrobot.eventbus.EventBus
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [(MainStoreModule::class)])
class StoreModule {

    @Provides
    @Singleton internal fun provideSessionStore(dispatcher: Dispatcher, bus: EventBus, preferences: Preferences, tracker: Tracker): SessionStore = SessionStoreImpl(dispatcher, bus, preferences, tracker)

    @Provides
    @Singleton internal fun providePunchStore(dispatcher: Dispatcher, bus: EventBus, preferences: Preferences, context: Context, @Named("main_dao_session") daoSession: DaoSession): PunchStore =
            MainPunchStoreImpl(dispatcher, bus, preferences, context, daoSession)

    @Provides
    @Singleton internal fun provideTimeOffStore(dispatcher: Dispatcher, bus: EventBus): TimeOffStore = TimeOffStoreImpl(dispatcher, bus)
}
