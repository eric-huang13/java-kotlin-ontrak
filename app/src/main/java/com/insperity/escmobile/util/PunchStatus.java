package com.insperity.escmobile.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.insperity.escmobile.util.PunchStatus.ARCHIVED_STATUS;
import static com.insperity.escmobile.util.PunchStatus.NOT_SYNCED_STATUS;
import static com.insperity.escmobile.util.PunchStatus.SYNCED_STATUS;

/**
 * Created by dxsier on 1/6/17.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({NOT_SYNCED_STATUS, SYNCED_STATUS, ARCHIVED_STATUS})
public @interface PunchStatus {
    int NOT_SYNCED_STATUS = 0;
    int SYNCED_STATUS = 1;
    int ARCHIVED_STATUS = 2;
}
