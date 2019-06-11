package com.vitaxa.jasteambot.socket;

import java.io.IOException;

public final class RequestException extends IOException {
    private static final long serialVersionUID = 7558237657082664821L;

    public RequestException(String message) {
        super(message);
    }

    public RequestException(Throwable exc) {
        super(exc);
    }

    public RequestException(String message, Throwable exc) {
        super(message, exc);
    }

    public String toString() {
        return getMessage();
    }
}
