package com.insperity.escmobile.net.gson;

import java.util.List;

/**
 * Represents a time off request to be submitted.
 */
public class TimeOffSubmitRequest {
    public final String comment;
    public final List<Integer> recipientUsers;
    public final List<TimeOffRequestDate> requestDates;

    public TimeOffSubmitRequest(String comment, List<Integer> recipientUsers, List<TimeOffRequestDate> requestDates) {
        this.comment = comment;
        this.recipientUsers = recipientUsers;
        this.requestDates = requestDates;
    }
}
