package com.delphiaconsulting.timestar.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.event.OnTimeOffRequestDetailsReceived
import com.delphiaconsulting.timestar.view.adapter.TimeOffRequestDetailAdapter
import kotlinx.android.synthetic.main.fragment_time_off_request_details.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class TimeOffRequestDetailsFragment : BaseFragment() {

    companion object {
        fun newInstance(): TimeOffRequestDetailsFragment = TimeOffRequestDetailsFragment()
    }

    @Inject lateinit var bus: EventBus

    private var adapter: TimeOffRequestDetailAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_time_off_request_details, container, false)

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
        super.onDestroy()
        bus.removeStickyEvent(OnTimeOffRequestDetailsReceived::class.java)
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeOffRequestDetailsReceived) {
        context?.let {
            adapter = adapter ?: TimeOffRequestDetailAdapter(it.applicationContext, event.requestDetails)
            setupDetailsList()
        }
    }

    private fun setupDetailsList() {
        if (requestDetailsList.adapter == null) {
            requestDetailsList.adapter = adapter
        }
    }
}
