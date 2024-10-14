package com.ziumks.bulkApiServer.util.exception;

public class HttpConnectionException extends Exception {
    private static final long serialVersionUID = 7237428832542072206L;

    public HttpConnectionException() {
    }

    public HttpConnectionException(String msg) {
        super(msg);
    }
}