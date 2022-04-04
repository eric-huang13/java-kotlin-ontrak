package com.delphiaconsulting.timestar.view.fragment

import com.delphiaconsulting.timestar.event.OnTimeEntrySummaryReceived
import org.greenrobot.eventbus.Subscribe

class TimeEntrySummaryFragment : TimeEntryBaseFragment() {

    companion object {
        fun newInstance() = TimeEntrySummaryFragment()
    }

    override val trackerPage: String
        get() = "Summary"

    override fun registerInBus() = bus.register(this)

    override fun unregisterFromBus() = bus.unregister(this)

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeEntrySummaryReceived) = setupRecyclerView(event.properties, event.items)
}