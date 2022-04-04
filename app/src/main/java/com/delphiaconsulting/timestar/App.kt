package com.delphiaconsulting.timestar

import android.content.Context
import android.os.Build
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import android.webkit.CookieSyncManager
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.delphiaconsulting.timestar.dagger.components.AppComponent
import com.delphiaconsulting.timestar.dagger.components.DaggerAppComponent
import com.delphiaconsulting.timestar.dagger.modules.AppModule
import com.delphiaconsulting.timestar.dagger.modules.ContextModule
import com.delphiaconsulting.timestar.dagger.modules.NetModule
import com.delphiaconsulting.timestar.dagger.modules.StoreModule
import com.delphiaconsulting.timestar.view.service.OfflinePunchJobService
import io.fabric.sdk.android.Fabric
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber


open class App : MultiDexApplication() {

    lateinit var component: AppComponent

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        Fabric.with(Fabric.Builder(this)
                .kits(Crashlytics())
                .debuggable(true)
                .build())

        BuildConfig.BUILD_ID?.let { Crashlytics.setInt("Build Id", it) }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this)
        }

        createComponent()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Stetho.initializeWithDefaults(this)
        }

        JodaTimeAndroid.init(this)

        OfflinePunchJobService.schedule(this)
    }

    protected fun createComponent() {
        component = DaggerAppComponent.builder()
                .contextModule(ContextModule(this))
                .appModule(AppModule())
                .netModule(NetModule())
                .storeModule(StoreModule())
                .build()
    }
}