package com.delphiaconsulting.timestar.dagger.modules

import android.content.Context
import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.store.LegalStore
import com.delphiaconsulting.timestar.store.TimeEntryStore
import com.delphiaconsulting.timestar.store.impl.LegalStoreImpl
import com.delphiaconsulting.timestar.store.impl.TimeEntryStoreImpl
import com.delphiaconsulting.timestar.util.AppUtil
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