package com.delphiaconsulting.timestar.net.gson;

/**
 * Created by dxsier on 1/6/17.
 */
public class ServerTime {
    public final long time;
    public final String dateTime;

    public ServerTime(long time, String dateTime) {
        this.time = time;
        this.dateTime = dateTime;
    }
}
