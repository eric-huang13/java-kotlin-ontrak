package com.insperity.escmobile.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.insperity.escmobile.util.PunchMode.NO_PUNCH_MODE;
import static com.insperity.escmobile.util.PunchMode.PUNCH_OFFLINE_MODE;
import static com.insperity.escmobile.util.PunchMode.PUNCH_ONLINE_MODE;

/**
 * Created by dxsier on 1/6/17.
 */

@Retention(RetentionPolicy.SOURCE)
@IntDef({NO_PUNCH_MODE, PUNCH_ONLINE_MODE, PUNCH_OFFLINE_MODE})
public @interface PunchMode {
    int NO_PUNCH_MODE = 0;
    int PUNCH_ONLINE_MODE = 1;
    int PUNCH_OFFLINE_MODE = 2;
}
