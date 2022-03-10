package com.insperity.escmobile.net.gson;

public class Response<T> {
    public final T data;
    public final ErrorMessage message;

    public Response(T data, ErrorMessage message) {
        this.data = data;
        this.message = message;
    }
}