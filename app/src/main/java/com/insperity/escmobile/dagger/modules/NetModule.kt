package com.insperity.escmobile.dagger.modules

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.insperity.escmobile.BuildConfig
import com.insperity.escmobile.net.service.SessionService
import com.insperity.escmobile.net.service.TimeEntryService
import com.insperity.escmobile.net.service.TimeOffService
import com.insperity.escmobile.net.service.TimePunchService
import com.insperity.escmobile.util.FirebaseLoggingInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(includes = [(MainNetModule::class)])
class NetModule {

    companion object {
        private val DEBUG_CONNECT_TIMEOUT: Long = 20
        private val DEBUG_READ_TIMEOUT: Long = 40
        private val RELEASE_CONNECT_TIMEOUT: Long = 20
        private val RELEASE_READ_TIMEOUT: Long = 20
    }

    @Provides
    @Singleton internal fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(if (BuildConfig.DEBUG) DEBUG_CONNECT_TIMEOUT else RELEASE_CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(if (BuildConfig.DEBUG) DEBUG_READ_TIMEOUT else RELEASE_READ_TIMEOUT, TimeUnit.SECONDS)
            .addNetworkInterceptor(StethoInterceptor())
            .addInterceptor(HttpLoggingInterceptor().setLevel(if (BuildConfig.DEBUG) BODY else BASIC))
            .addInterceptor(FirebaseLoggingInterceptor())
            .build()

    @Provides
    @Singleton internal fun provideSessionService(retrofit: Retrofit): SessionService = retrofit.create(SessionService::class.java)

    @Provides
    @Singleton internal fun provideTimeOffService(retrofit: Retrofit): TimeOffService = retrofit.create(TimeOffService::class.java)

    @Provides
    @Singleton internal fun provideTimePunchService(retrofit: Retrofit): TimePunchService = retrofit.create(TimePunchService::class.java)

    @Provides
    @Singleton internal fun provideTimeEntryService(retrofit: Retrofit): TimeEntryService = retrofit.create(TimeEntryService::class.java)
}