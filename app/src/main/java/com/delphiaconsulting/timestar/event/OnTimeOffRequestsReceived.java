package com.delphiaconsulting.timestar.event;

import com.delphiaconsulting.timestar.net.gson.TimeOffRequests;

/**
 * Created by qktran on 12/28/16.
 */
public class OnTimeOffRequestsReceived {
    public final TimeOffRequests timeOffRequests;

    public OnTimeOffRequestsReceived(TimeOffRequests timeOffRequests) {
        this.timeOffRequests = timeOffRequests;
    }
}
