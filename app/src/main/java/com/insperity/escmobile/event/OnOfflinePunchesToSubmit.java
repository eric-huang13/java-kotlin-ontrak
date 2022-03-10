package com.insperity.escmobile.event;

import com.insperity.escmobile.net.gson.PunchesToSubmit;

/**
 * Created by dxsier on 1/9/17.
 */
public class OnOfflinePunchesToSubmit {
    public final PunchesToSubmit punchesToSubmit;

    public OnOfflinePunchesToSubmit(PunchesToSubmit punchesToSubmit) {
        this.punchesToSubmit = punchesToSubmit;
    }
}
