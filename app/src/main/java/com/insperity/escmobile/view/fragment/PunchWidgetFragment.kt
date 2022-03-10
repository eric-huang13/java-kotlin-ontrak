package com.insperity.escmobile.view.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnLatestPunchFound
import com.insperity.escmobile.event.OnNoPunchesAvailableError
import com.insperity.escmobile.event.OnOfflinePunchesSynced
import com.insperity.escmobile.event.OnServerTimeTicked
import com.insperity.escmobile.store.PunchStore
import com.insperity.escmobile.util.PunchStatus
import com.insperity.escmobile.view.activity.PunchActivity
import kotlinx.android.synthetic.main.fragment_punch_widget.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class PunchWidgetFragment : BaseFragment() {

    companion object {
        val TAG: String = PunchWidgetFragment::class.java.simpleName

        fun newInstance() = PunchWidgetFragment()
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var store: PunchStore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_punch_widget, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
        dateText.text = DateTimeFormat.forPattern("EEE, MMMMM dd, yyyy").print(DateTime.now())
        digitalClock.setTimeTextColor(R.color.insperity_green)
        store.getLatestPunch()
        widgetContainer.setOnClickListener {
            startActivity(PunchActivity.getCallingIntent(activity))
            activity?.overridePendingTransition(0, 0)
            activity?.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }

    @Subscribe
    fun onEvent(event: OnServerTimeTicked) {
        analogClock.setTime(event.time)
    }

    @Subscribe
    fun onEvent(event: OnOfflinePunchesSynced) {
        store.getLatestPunch()
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnLatestPunchFound) {
        latestPunchContainer.visibility = View.VISIBLE
        noRecentText.visibility = View.GONE
        punchTypeText.text = event.punch.punchCategory.description
        punchTimeText.text = timeFormatter(event.punch.datetime)
        bus.removeStickyEvent(event)
        if (event.punch.syncStatus != PunchStatus.ARCHIVED_STATUS) {
            statusImage.setImageResource(if (event.punch.syncStatus == PunchStatus.NOT_SYNCED_STATUS) R.drawable.ic_punch_not_synced else R.drawable.ic_punch_synced)
            return
        }
        if (event.punch.comment.isEmpty()) {
            statusImage.visibility = View.INVISIBLE
        }
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnNoPunchesAvailableError) {
        bus.removeStickyEvent(event)
    }

    private fun timeFormatter(timestamp: Long) = DateTime(timestamp).toString(DateTimeFormat.forPattern("hh:mm aa"))
}