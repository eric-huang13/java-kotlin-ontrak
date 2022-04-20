package com.delphiaconsulting.timestar.view.activity

import android.graphics.Typeface
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.avast.android.dialogs.iface.ISimpleDialogListener
import com.delphiaconsulting.timestar.App
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.dagger.components.AppComponent
import com.delphiaconsulting.timestar.event.OnUnauthorizedAccess
import com.delphiaconsulting.timestar.net.analytics.Tracker
import com.delphiaconsulting.timestar.util.Preferences
import com.delphiaconsulting.timestar.util.PunchMode
import kotlinx.android.synthetic.main.content_selector_spinner.*
import kotlinx.android.synthetic.main.nav_drawer.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by dxsier on 11/18/16.
 */

abstract class BaseActivity : AppCompatActivity(), ISimpleDialogListener {

    companion object {
        private const val DRAWER_LAUNCH_DELAY: Long = 250
        private const val MAIN_CONTENT_FADEOUT_DURATION: Long = 150

        protected const val DRAWER_ITEM_INVALID = -2
        private const val DRAWER_ITEM_SEPARATOR = -1
        const val DRAWER_ITEM_DASHBOARD = 0
        const val DRAWER_ITEM_PUNCHES = 1
        const val DRAWER_ITEM_TIME_ENTRY = 2
        const val DRAWER_ITEM_TIME_MANAGEMENT = 3
        const val DRAWER_ITEM_TIME_OFF = 4
        const val DRAWER_ITEM_PTO_APPROVALS = 5
        const val DRAWER_ITEM_ABOUT = 6

        private val DRAWER_TITLE_RES_ID = intArrayOf(R.string.drawer_dashboard_title, R.string.drawer_punch_title, R.string.drawer_time_entry_title, R.string.drawer_time_management_title, R.string.drawer_time_off_title, R.string.drawer_pto_approvals_title, R.string.drawer_about_title)
        private val DRAWER_ICON_RES_ID = intArrayOf(R.drawable.ic_dashboard, R.drawable.ic_punches, R.drawable.ic_timesheet, R.drawable.ic_time_management, R.drawable.ic_time_off_request, R.drawable.ic_time_off_approval, 0)
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var tracker: Tracker
    @Inject lateinit var preferences: Preferences

    private var drawerLayout: DrawerLayout? = null
    private val drawerItemList = ArrayList<Int>()
    private val drawerItemViewList = ArrayList<View>()

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        appComponent.inject(this)
        setupToolbar()
        setupDrawer()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        tracker.trackScreenView(trackerScreen)
    }

    override fun onResume() {
        super.onResume()
        registerInBus()
    }

    override fun onPause() {
        unregisterFromBus()
        super.onPause()
    }

    override fun onBackPressed() {
        if (isDrawerOpen) {
            closeDrawer()
            return
        }
        if (selfDrawerItem != DRAWER_ITEM_INVALID && selfDrawerItem != DRAWER_ITEM_DASHBOARD) {
            startActivity(DashboardActivity.getCallingIntent(this))
        }
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }

    protected open fun registerInBus() = bus.register(this)

    protected open fun unregisterFromBus() = bus.unregister(this)

    @Subscribe
    fun onEvent(event: OnUnauthorizedAccess) {
        preferences.timeStarToken = ""
        startActivity(OnboardingActivity.getCallingIntent(this))
        finish()
    }

    protected open fun setupToolbar() {
        toolbar?.let {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.setTitle(titleResource)
            if (selfDrawerItem != DRAWER_ITEM_INVALID) {
                supportActionBar?.setHomeAsUpIndicator(DrawerArrowDrawable(this))
            }
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setupToolbarListener()
        }
    }

    protected open fun setupToolbarListener() {
        if (selfDrawerItem == DRAWER_ITEM_INVALID) return
        toolbar?.setNavigationOnClickListener {
            if (isDrawerOpen) {
                closeDrawer()
                return@setNavigationOnClickListener
            }
            openDrawer()
        }
    }

    protected abstract val titleResource: Int

    protected abstract val trackerScreen: String

    protected open val selfDrawerItem: Int
        get() = DRAWER_ITEM_INVALID

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        drawerLayout?.let {
            if (selfDrawerItem == DRAWER_ITEM_INVALID) {
                navDrawer?.let { (it.parent as ViewGroup).removeView(it) }
                return
            }

            it.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
            populateDrawer()
        }
    }

    protected fun populateDrawer() {
        drawerItemList.clear()
        drawerItemList.add(DRAWER_ITEM_DASHBOARD)
        if (preferences.punchMode != PunchMode.NO_PUNCH_MODE) drawerItemList.add(DRAWER_ITEM_PUNCHES)
        if (preferences.timeEntryEnabled) drawerItemList.add(DRAWER_ITEM_TIME_ENTRY)
        if (preferences.timeManagementEnabled) drawerItemList.add(DRAWER_ITEM_TIME_MANAGEMENT)
        if (preferences.timeOffRequestEnabled) drawerItemList.add(DRAWER_ITEM_TIME_OFF)
        if (preferences.timeOffApprovalEnabled) drawerItemList.add(DRAWER_ITEM_PTO_APPROVALS)

        //??????
//        drawerItemList.add(DRAWER_ITEM_PUNCHES)
//        drawerItemList.add(DRAWER_ITEM_TIME_ENTRY)
//        drawerItemList.add(DRAWER_ITEM_TIME_MANAGEMENT)
//        drawerItemList.add(DRAWER_ITEM_TIME_OFF)
//        drawerItemList.add(DRAWER_ITEM_PTO_APPROVALS)
        //??????

        drawerItemList.add(DRAWER_ITEM_SEPARATOR)
        drawerItemList.add(DRAWER_ITEM_ABOUT)
        createDrawerItems()
    }

    private fun createDrawerItems() = drawerItemsListContainer?.let { drawerContainer ->
        drawerContainer.removeAllViews()
        drawerItemViewList.clear()
        drawerItemViewList.addAll(drawerItemList.asSequence().map { createDrawerItem(it, drawerContainer) }.toMutableList())
        drawerItemViewList.forEach { drawerContainer.addView(it) }
    }

    private fun createDrawerItem(itemId: Int, container: ViewGroup): View {
        val layoutToInflate = if (isSeparator(itemId)) R.layout.drawer_separator else R.layout.drawer_item
        val view = layoutInflater.inflate(layoutToInflate, container, false)
        if (isSeparator(itemId)) {
            return view
        }
        val titleText = view.findViewById<TextView>(R.id.drawerItemTitle)
        val titleId = if (itemId >= 0 && itemId < DRAWER_TITLE_RES_ID.size) DRAWER_TITLE_RES_ID[itemId] else 0
        titleText.setText(titleId)
        val iconView = view.findViewById<ImageView>(R.id.drawerItemIcon)
        if (DRAWER_ICON_RES_ID[itemId] != 0) {
            iconView.setImageResource(DRAWER_ICON_RES_ID[itemId])
        } else {
            iconView.visibility = View.GONE
        }
        val selected = selfDrawerItem == itemId
        formatDrawerItem(view, selected)
        view.setOnClickListener { onDrawerItemClicked(itemId) }
        return view
    }

    private fun formatDrawerItem(view: View, selected: Boolean) {
        val titleText = view.findViewById<TextView>(R.id.drawerItemTitle)
        titleText.setTextColor(ContextCompat.getColor(this, if (selected) R.color.orange_color else R.color.text_grey))
        titleText.setTypeface(titleText.typeface, if (selected) Typeface.BOLD else Typeface.NORMAL)
        val iconView = view.findViewById<ImageView>(R.id.drawerItemIcon)
        iconView.setColorFilter(ContextCompat.getColor(this, if (selected) R.color.orange_color else R.color.text_grey))
    }

    private fun onDrawerItemClicked(itemId: Int) {
        closeDrawer()
        if (itemId == selfDrawerItem) {
            return
        }

        Observable.timer(DRAWER_LAUNCH_DELAY, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { goToDrawerItem(itemId) }

        formatSelectedDrawerItem(itemId)
        val contentContainer = findViewById<ViewGroup?>(R.id.contentContainer)
        contentContainer?.let { it.animate().alpha(0f).duration = MAIN_CONTENT_FADEOUT_DURATION }
    }

    private fun formatSelectedDrawerItem(itemId: Int) = drawerItemViewList.indices
            .filter { it < drawerItemList.size && !isSeparator(drawerItemList[it]) }
            .forEach { formatDrawerItem(drawerItemViewList[it], itemId == drawerItemList[it]) }

    private fun goToDrawerItem(item: Int) {
        val intent = when (item) {
            DRAWER_ITEM_DASHBOARD -> DashboardActivity.getCallingIntent(this)
            DRAWER_ITEM_PUNCHES -> PunchActivity.getCallingIntent(this)
            DRAWER_ITEM_PTO_APPROVALS -> MainTimeOffApprovalsActivity.getCallingIntent(this)
            DRAWER_ITEM_TIME_OFF -> MainTimeOffRequestsActivity.getCallingIntent(this)
            DRAWER_ITEM_TIME_ENTRY -> MainTimeEntryActivity.getCallingIntent(this, clazz = TimeEntryMenuActivity::class.java)
            DRAWER_ITEM_TIME_MANAGEMENT -> MainTimeEntrySupervisorActivity.getCallingIntent(this)
            DRAWER_ITEM_ABOUT -> AboutActivity.getCallingIntent(this)
            else -> DashboardActivity.getCallingIntent(this)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    private fun isSeparator(itemId: Int) = itemId == DRAWER_ITEM_SEPARATOR

    protected fun openDrawer() = drawerLayout?.openDrawer(GravityCompat.START)

    protected fun closeDrawer() = drawerLayout?.closeDrawer(GravityCompat.START)

    protected val isDrawerOpen: Boolean
        get() = drawerLayout != null && drawerLayout!!.isDrawerOpen(GravityCompat.START)

    protected val isOrientationLandscape: Boolean
        get() = resources.getBoolean(R.bool.is_landscape)

    protected val appComponent: AppComponent
        get() = (applicationContext as App).component

    /**
     * Progress bar logic
     */
    fun showProgressBar(show: Boolean) {
        progressBarContainer?.visibility = if (show) VISIBLE else GONE
    }

    protected open fun setupContentSelectorSpinner(contentSelectorAdapter: ArrayAdapter<*>, onItemSelectedAction: (Int) -> Unit, hideIfSingleItem: Boolean = true) {
        if (contentSelectorSpinner == null) return
        contentSelectorAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        contentSelectorSpinner.adapter = contentSelectorAdapter
        contentSelectorSpinner.visibility = if (contentSelectorSpinner.count < 1 || (contentSelectorSpinner.count == 1 && hideIfSingleItem)) GONE else VISIBLE
        contentSelectorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) = onItemSelectedAction(position)

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onNegativeButtonClicked(requestCode: Int) {}

    override fun onPositiveButtonClicked(requestCode: Int) {}

    override fun onNeutralButtonClicked(requestCode: Int) {}
}
