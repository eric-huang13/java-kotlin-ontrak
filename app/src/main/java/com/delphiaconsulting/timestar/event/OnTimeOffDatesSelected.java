package com.delphiaconsulting.timestar.event;

import com.delphiaconsulting.timestar.net.gson.TimeOffRequestDate;

import java.util.List;

/**
 * Created by dxsier on 2/2/17.
 */
public class OnTimeOffDatesSelected {
    public final List<TimeOffRequestDate> requestDates;

    public OnTimeOffDatesSelected(List<TimeOffRequestDate> requestDates) {
        this.requestDates = requestDates;
    }
}
