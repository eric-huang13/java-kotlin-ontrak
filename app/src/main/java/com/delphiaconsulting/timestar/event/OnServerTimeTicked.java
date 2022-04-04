package com.delphiaconsulting.timestar.event;

/**
 * Created by dxsier on 1/6/17.
 */
public class OnServerTimeTicked {
    public final long time;

    public OnServerTimeTicked(long time) {
        this.time = time;
    }
}
