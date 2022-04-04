package com.delphiaconsulting.timestar.view.widget

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import com.delphiaconsulting.timestar.App
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.data.OrgDefaultEntity
import com.delphiaconsulting.timestar.data.OrgItemEntity
import com.delphiaconsulting.timestar.data.OrgLevelEntity
import com.delphiaconsulting.timestar.event.OnOrgLevelChanged
import com.delphiaconsulting.timestar.event.OnPunchOrgItemSelected
import com.delphiaconsulting.timestar.event.OnSelectOrgLevel
import com.delphiaconsulting.timestar.event.OnTrackPunchEvent
import com.delphiaconsulting.timestar.net.analytics.Tracker
import com.delphiaconsulting.timestar.store.PunchStore
import com.delphiaconsulting.timestar.util.Preferences
import com.delphiaconsulting.timestar.view.activity.MainOrgItemsSearchActivity
import com.delphiaconsulting.timestar.view.adapter.PunchOrgLevelAdapter
import com.delphiaconsulting.timestar.view.extension.onItemSelected
import kotlinx.android.synthetic.main.view_punch_org_level.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import rx.Observable
import java.util.*
import javax.inject.Inject


class PunchOrgLevelView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    @Inject lateinit var bus: EventBus
    @Inject lateinit var store: PunchStore
    @Inject lateinit var tracker: Tracker
    @Inject lateinit var preferences: Preferences

    private lateinit var orgLevel: OrgLevelEntity
    private lateinit var adapter: PunchOrgLevelAdapter
    private var nextPunchOrgLevelView: PunchOrgLevelView? = null
    private var isMainDefault: Boolean = false

    init {
        (context.applicationContext as App).component.inject(this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        orgItemsSpinner.onItemSelected { _, position -> onOrgItemsSpinnerItemSelected(position) }
        if (preferences.quickOrgLevelSelection) return
        orgItemsSpinner.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                context.startActivity(MainOrgItemsSearchActivity.getCallingIntent(context))
                (context as AppCompatActivity).overridePendingTransition(R.anim.slide_up, R.anim.stay)
                bus.postSticky(OnSelectOrgLevel(orgLevel, adapter.orgItems))
            }
            return@setOnTouchListener true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bus.register(this)
    }

    override fun onDetachedFromWindow() {
        bus.unregister(this)
        super.onDetachedFromWindow()
    }

    @Subscribe
    fun onEvent(event: OnPunchOrgItemSelected) {
        if (event.orgLevelId != orgLevel.id) return
        setOrgItemsSpinnerSelection(event.selectedPosition)
    }

    fun setOrgLevel(orgLevel: OrgLevelEntity) {
        this.orgLevel = orgLevel
        orgLevelTitleText.text = orgLevel.name
    }

    fun setNextPunchOrgLevelView(nextPunchOrgLevelView: PunchOrgLevelView?) {
        this.nextPunchOrgLevelView = nextPunchOrgLevelView
    }

    fun setupOrgItemsSpinner(visibleOrgItems: List<Long>?) = Observable.from(orgLevel.orgItems)
            .concatWith(Observable.just(OrgItemEntity(0L, context.getString(R.string.n_a_punch_text), orgLevel.id, "", 0)))
            .filter { visibleOrgItems == null || visibleOrgItems.contains(it.id) || it.id == 0L }
            .toList()
            .toBlocking()
            .subscribe {
                adapter = PunchOrgLevelAdapter(context, it)
                orgItemsSpinner.adapter = adapter
                setOrgItemsSpinnerSelection(0)
            }

    fun setDefaultOrgItemSelection(orgDefault: OrgDefaultEntity?, isMainDefault: Boolean = true) {
        this.isMainDefault = isMainDefault
        if (orgDefault != null) {
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i)?.id == orgDefault.orgItemId) {
                    setOrgItemsSpinnerSelection(i)
                    return
                }
            }
        }
        setOrgItemsSpinnerSelection(getNAItemPosition())
    }

    private fun getNAItemPosition(): Int {
        val onlyNA = adapter.count == 1 && adapter.getItem(0)?.id == 0L
        return if (onlyNA) 0 else adapter.count
    }

    private fun setOrgItemsSpinnerSelection(position: Int) {
        orgItemsSpinner.tag = position
        orgItemsSpinner.setSelection(position, true)
    }

    private fun onOrgItemsSpinnerItemSelected(position: Int) {
        val selectedOrgItem = orgItemsSpinner.selectedItem as OrgItemEntity
        if (orgItemsSpinner.tag != position) {
            bus.post(OnOrgLevelChanged())
            bus.post(OnTrackPunchEvent(false, "Change Org", String.format(Locale.US, "%d:%d:%s", orgLevel.id, selectedOrgItem.id, selectedOrgItem.label)))
            orgItemsSpinner.tag = position
        }
        nextPunchOrgLevelView?.let {
            it.setupOrgItemsSpinner(selectedOrgItem.nextOrgItemIds)
            val nextOrgDefault = store.getNextDefaultFor(orgLevel.id, selectedOrgItem.id, isMainDefault)
            it.setDefaultOrgItemSelection(nextOrgDefault, isMainDefault)
            isMainDefault = false
        }
    }

    fun getOrgLevelSelections(): List<Long> {
        val orgLevelSelections = ArrayList<Long>()
        orgLevelSelections.add(orgLevel.id)
        val orgItemEntity = orgItemsSpinner.selectedItem as OrgItemEntity?
        orgLevelSelections.add(if (orgItemEntity == null) 0L else orgItemEntity.id)
        nextPunchOrgLevelView?.let { orgLevelSelections.addAll(it.getOrgLevelSelections()) }
        return orgLevelSelections
    }
}
