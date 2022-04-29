package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.errors;

public class ResponseTimeoutError extends SenderError {
    public ResponseTimeoutError() {
        super("response time-outed");
    }

    public ResponseTimeoutError(String message) {
        super(message);
    }
}
