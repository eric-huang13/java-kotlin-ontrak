package com.insperity.escmobile.net.gson;

import java.util.List;

/**
 * Created by dxsier on 1/9/17.
 */
public class PunchesToSubmit {
    public final List<PunchToSubmit> punches;

    public PunchesToSubmit(List<PunchToSubmit> punches) {
        this.punches = punches;
    }
}
