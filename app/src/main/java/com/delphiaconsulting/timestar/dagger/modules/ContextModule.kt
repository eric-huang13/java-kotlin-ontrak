package com.delphiaconsulting.timestar.dagger.modules

import android.content.Context
import com.delphiaconsulting.timestar.App
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule(private val app: App) {

    @Provides
    @Singleton internal fun provideApplicationContext(): Context = app
}