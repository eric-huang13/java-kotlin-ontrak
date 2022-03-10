package com.insperity.escmobile.event;

import com.insperity.escmobile.net.gson.TimeOffRequests;

/**
 * Created by qktran on 12/28/16.
 */
public class OnTimeOffRequestsReceived {
    public final TimeOffRequests timeOffRequests;

    public OnTimeOffRequestsReceived(TimeOffRequests timeOffRequests) {
        this.timeOffRequests = timeOffRequests;
    }
}
