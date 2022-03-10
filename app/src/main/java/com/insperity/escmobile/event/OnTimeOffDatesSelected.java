package com.insperity.escmobile.event;

import com.insperity.escmobile.net.gson.TimeOffRequestDate;

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
