package com.delphiaconsulting.timestar.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.event.OnTimeOffRequestDetailsReceived
import com.delphiaconsulting.timestar.util.AppUtil
import com.delphiaconsulting.timestar.util.StringUtil
import com.delphiaconsulting.timestar.view.common.MonthDecorator
import com.squareup.timessquare.CalendarCellDecorator
import com.squareup.timessquare.CalendarPickerView
import kotlinx.android.synthetic.main.fragment_time_off_calendar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import rx.Observable
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by dxsier on 07/12/17.
 */
class TimeOffCalendarFragment : BaseFragment() {

    companion object {
        fun newInstance() = TimeOffCalendarFragment()
    }

    @Inject lateinit var bus: EventBus
    @Inject lateinit var appUtil: AppUtil

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_time_off_calendar, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
        calendarPicker.dividerHeight = appUtil.dpToPx(25f)
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnTimeOffRequestDetailsReceived) {
        Observable.from(event.requestDetails.content.request.requestDates)
                .map { StringUtil.parseDateToCalendar(it.effectiveDate.value) }
                .toList()
                .subscribe { setupCalendar(it) }
    }

    private fun setupCalendar(dates: List<Date>) {
        val allDates = ArrayList(dates)
        allDates.add(DateTime.now().dayOfYear().withMinimumValue().toDate())
        allDates.add(DateTime.now().plusYears(1).dayOfYear().withMaximumValue().toDate())
        calendarPicker.init(Collections.min(allDates), Collections.max(allDates))
                .inMode(CalendarPickerView.SelectionMode.MULTIPLE).withSelectedDates(dates)
        val decorators = ArrayList<CalendarCellDecorator>()
        decorators.add(MonthDecorator(context))
        calendarPicker.decorators = decorators
        calendarPicker.setCellClickInterceptor { true }
        previousMonthButton.setOnClickListener { calendarPicker.setSelection(calendarPicker.firstVisiblePosition - 1) }
        nextMonthButton.setOnClickListener { calendarPicker.setSelection(calendarPicker.firstVisiblePosition + 1) }
    }
}
