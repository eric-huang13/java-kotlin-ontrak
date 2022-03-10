package com.insperity.escmobile.dagger.components


import com.insperity.escmobile.dagger.modules.AppModule
import com.insperity.escmobile.dagger.modules.ContextModule
import com.insperity.escmobile.dagger.modules.NetModule
import com.insperity.escmobile.dagger.modules.StoreModule
import com.insperity.escmobile.view.activity.BaseActivity
import com.insperity.escmobile.view.activity.OnboardingActivity
import com.insperity.escmobile.view.activity.PunchActivity
import com.insperity.escmobile.view.activity.SplashActivity
import com.insperity.escmobile.view.fragment.PunchWidgetFragment
import com.insperity.escmobile.view.fragment.TimeOffApprovalWidgetFragment
import com.insperity.escmobile.view.fragment.TimeOffRequestWidgetFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(ContextModule::class), (AppModule::class), (NetModule::class), (StoreModule::class)])
interface AppComponent : MainAppComponent {

    fun inject(splashActivity: SplashActivity)
    fun inject(onboardingActivity: OnboardingActivity)
    fun inject(baseActivity: BaseActivity)
    fun inject(punchWidgetFragment: PunchWidgetFragment)
    fun inject(timeOffRequestWidgetFragment: TimeOffRequestWidgetFragment)
    fun inject(timeOffApprovalWidgetFragment: TimeOffApprovalWidgetFragment)
}
