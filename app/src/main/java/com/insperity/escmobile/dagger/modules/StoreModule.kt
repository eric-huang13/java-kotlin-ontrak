package com.insperity.escmobile.dagger.modules

import android.content.Context
import com.insperity.escmobile.data.DaoSession
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.net.analytics.Tracker
import com.insperity.escmobile.store.*
import com.insperity.escmobile.store.impl.*
import com.insperity.escmobile.util.Preferences
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
