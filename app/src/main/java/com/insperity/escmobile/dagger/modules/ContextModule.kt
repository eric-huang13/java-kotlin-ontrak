package com.insperity.escmobile.dagger.modules

import android.content.Context
import com.insperity.escmobile.App
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule(private val app: App) {

    @Provides
    @Singleton internal fun provideApplicationContext(): Context = app
}