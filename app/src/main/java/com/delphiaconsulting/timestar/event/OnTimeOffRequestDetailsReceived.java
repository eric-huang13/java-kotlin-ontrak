package com.delphiaconsulting.timestar.event;

import com.delphiaconsulting.timestar.net.gson.TimeOffRequestDetails;

/**
 * Created by qktran on 1/25/17.
 */
public class OnTimeOffRequestDetailsReceived {
    public final TimeOffRequestDetails requestDetails;

    public OnTimeOffRequestDetailsReceived(TimeOffRequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }
}
