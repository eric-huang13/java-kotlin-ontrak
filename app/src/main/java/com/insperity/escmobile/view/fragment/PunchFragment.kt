package com.insperity.escmobile.view.fragment


import android.animation.LayoutTransition
import android.animation.TimeInterpolator
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.insperity.escmobile.R
import com.insperity.escmobile.event.*
import com.insperity.escmobile.util.Preferences
import com.insperity.escmobile.view.service.PunchDataService
import kotlinx.android.synthetic.main.fragment_punch.*
import kotlinx.android.synthetic.main.progress_bar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.Subscriptions
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PunchFragment : BaseFragment() {

    companion object {
        fun newInstance() = PunchFragment()
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var preferences: Preferences

    private var subscription = Subscriptions.empty()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_punch, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentsContainer.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)

        showProgressBar(true)
        if (!PunchDataService.isServiceRunning(context!!)) {
            scheduleOverlayHideAction()
        }
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        subscription.unsubscribe()
        super.onDestroy()
    }

    private fun scheduleOverlayHideAction() {
        subscription = Observable.timer(800, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    fragmentsContainer.visibility = View.VISIBLE
                    showProgressBar(false)
                }
    }

    @Subscribe
    fun onEvent(event: OnPunchCategoriesLoaded) {
        if (event.punchCategories?.first?.isEmpty() == true || event.punchCategories?.second?.isEmpty() == true || tabLayout.tabCount > 0) return
        tabLayout.addTab(tabLayout.newTab().setText(R.string.punch_tab_text))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.transfer_tab_text))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) = bus.post(OnPunchCategoryTabTapped(tab?.position == 1))
        })
        tabLayout.visibility = View.VISIBLE
    }

    @Subscribe
    fun onEvent(event: OnPunchBaseDataServiceStopped) = scheduleOverlayHideAction()

    @Subscribe
    fun onEvent(event: OnNotifyPunchContainerTransition) {
        fragmentsContainer.layoutTransition.setStartDelay(LayoutTransition.CHANGING, if (event.disappearingAppearingViewsCount < 0) 600 else 150)
        fragmentsContainer.layoutTransition.setDuration(LayoutTransition.CHANGING, if (event.disappearingAppearingViewsCount < 0) 700 else 400)
        val interpolator: TimeInterpolator = if (event.disappearingAppearingViewsCount < 0) AccelerateInterpolator() else DecelerateInterpolator()
        fragmentsContainer.layoutTransition.setInterpolator(LayoutTransition.CHANGING, interpolator)
    }

    @Subscribe
    fun onEvent(event: OnShowProgressBar) {
        showProgressBar(event.show)
    }

    public override fun showProgressBar(show: Boolean) {
        if (progressBarContainer == null) return
        progressBarContainer.visibility = if (show) View.VISIBLE else View.GONE
    }
}
