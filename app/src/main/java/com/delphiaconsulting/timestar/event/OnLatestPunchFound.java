package com.delphiaconsulting.timestar.event;

import com.delphiaconsulting.timestar.data.PunchEntity;

/**
 * Created by dxsier on 3/20/17.
 */

public class OnLatestPunchFound {
    public final PunchEntity punch;

    public OnLatestPunchFound(PunchEntity punch) {
        this.punch = punch;
    }
}
