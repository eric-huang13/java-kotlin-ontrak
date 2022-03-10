package com.insperity.escmobile.event;

import com.insperity.escmobile.net.gson.TimeOffRequestDate;

/**
 * Created by dxsier on 2/7/17.
 */
public class OnTimeOffDateItemRemoved {
    public final int position;
    public final TimeOffRequestDate requestDate;

    public OnTimeOffDateItemRemoved(int position, TimeOffRequestDate requestDate) {
        this.position = position;
        this.requestDate = requestDate;
    }
}
