package com.insperity.escmobile.net.gson;

/**
 * Created by dxsier on 2/2/17.
 */

public class TimeOffRecipient {
    public final int id;
    public final String name;

    public TimeOffRecipient(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
