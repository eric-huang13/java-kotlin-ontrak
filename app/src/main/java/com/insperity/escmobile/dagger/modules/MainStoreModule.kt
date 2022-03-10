package com.insperity.escmobile.dagger.modules

import android.content.Context
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.store.LegalStore
import com.insperity.escmobile.store.TimeEntryStore
import com.insperity.escmobile.store.impl.LegalStoreImpl
import com.insperity.escmobile.store.impl.TimeEntryStoreImpl
import com.insperity.escmobile.util.AppUtil
import dagger.Module
import dagger.Provides
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

@Module
class MainStoreModule {

    @Provides
    @Singleton internal fun provideLegalStore(dispatcher: Dispatcher, bus: EventBus): LegalStore = LegalStoreImpl(dispatcher, bus)

    @Provides
    @Singleton internal fun provideTimeEntryStore(dispatcher: Dispatcher, bus: EventBus, context: Context, appUtil: AppUtil): TimeEntryStore = TimeEntryStoreImpl(dispatcher, bus, context, appUtil)
}