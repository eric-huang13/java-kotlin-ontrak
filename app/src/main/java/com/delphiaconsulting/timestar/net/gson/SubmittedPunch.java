package com.delphiaconsulting.timestar.net.gson;

/**
 * Created by dxsier on 1/6/17.
 */
public class SubmittedPunch {
    public final int punchId;
    public final int success;

    public SubmittedPunch(int punchId, int success) {
        this.punchId = punchId;
        this.success = success;
    }
}
