package com.insperity.escmobile.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnTimeEntryItemClicked
import com.insperity.escmobile.view.adapter.TimeEntryDetailDataAdapter
import kotlinx.android.synthetic.main.fragment_time_entry_detail.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class TimeEntryDetailFragment : BaseFragment() {

    companion object {
        fun newInstance() = TimeEntryDetailFragment()
    }

    @Inject lateinit var bus: EventBus

    private var adapter: TimeEntryDetailDataAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_time_entry_detail, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }

    override fun onDestroy() {
        bus.removeStickyEvent(OnTimeEntryItemClicked::class.java)
        super.onDestroy()
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeEntryItemClicked) {
        context?.let {
            if (adapter == null) {
                adapter = TimeEntryDetailDataAdapter(it, bus)
            }
            if (recyclerView.adapter == null) {
                recyclerView.adapter = adapter
            }
            adapter?.setData(event.detailItem.items)
        }
    }
}