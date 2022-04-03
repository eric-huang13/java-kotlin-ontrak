package com.insperity.escmobile

import android.content.Context
import android.os.Build
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import android.webkit.CookieSyncManager
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.insperity.escmobile.dagger.components.AppComponent
import com.insperity.escmobile.dagger.components.DaggerAppComponent
import com.insperity.escmobile.dagger.modules.AppModule
import com.insperity.escmobile.dagger.modules.ContextModule
import com.insperity.escmobile.dagger.modules.NetModule
import com.insperity.escmobile.dagger.modules.StoreModule
import com.insperity.escmobile.view.service.OfflinePunchJobService
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