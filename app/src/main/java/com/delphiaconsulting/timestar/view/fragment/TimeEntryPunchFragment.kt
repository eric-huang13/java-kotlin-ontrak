package com.delphiaconsulting.timestar.view.fragment

import com.delphiaconsulting.timestar.event.OnTimeEntryPunchesReceived
import org.greenrobot.eventbus.Subscribe

class TimeEntryPunchFragment : TimeEntryBaseFragment() {

    companion object {
        fun newInstance() = TimeEntryPunchFragment()
    }

    override val trackerPage: String
        get() = "Punch"

    override fun registerInBus() = bus.register(this)

    override fun unregisterFromBus() = bus.unregister(this)

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeEntryPunchesReceived) = setupRecyclerView(event.properties, event.items)
}