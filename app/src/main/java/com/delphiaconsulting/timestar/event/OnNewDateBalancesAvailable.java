package com.delphiaconsulting.timestar.event;

import android.util.SparseArray;

import java.util.Map;

/**
 * Created by dxsier on 2/17/17.
 */
public class OnNewDateBalancesAvailable {
    public final SparseArray<Map<String, Double>> balancesByBucket;

    public OnNewDateBalancesAvailable(SparseArray<Map<String, Double>> balancesByBucket) {
        this.balancesByBucket = balancesByBucket;
    }
}
