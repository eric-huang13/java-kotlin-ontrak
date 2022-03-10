package com.insperity.escmobile.event;

import com.insperity.escmobile.data.PunchEntity;

/**
 * Created by dxsier on 3/20/17.
 */

public class OnLatestPunchFound {
    public final PunchEntity punch;

    public OnLatestPunchFound(PunchEntity punch) {
        this.punch = punch;
    }
}
