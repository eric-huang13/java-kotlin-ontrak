package com.delphiaconsulting.timestar.dagger.components


import com.delphiaconsulting.timestar.dagger.modules.AppModule
import com.delphiaconsulting.timestar.dagger.modules.ContextModule
import com.delphiaconsulting.timestar.dagger.modules.NetModule
import com.delphiaconsulting.timestar.dagger.modules.StoreModule
import com.delphiaconsulting.timestar.view.activity.BaseActivity
import com.delphiaconsulting.timestar.view.activity.OnboardingActivity
import com.delphiaconsulting.timestar.view.activity.PunchActivity
import com.delphiaconsulting.timestar.view.activity.SplashActivity
import com.delphiaconsulting.timestar.view.fragment.PunchWidgetFragment
import com.delphiaconsulting.timestar.view.fragment.TimeOffApprovalWidgetFragment
import com.delphiaconsulting.timestar.view.fragment.TimeOffRequestWidgetFragment
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
