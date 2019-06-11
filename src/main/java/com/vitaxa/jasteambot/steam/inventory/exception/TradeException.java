package com.vitaxa.jasteambot.steam.inventory.exception;

public class TradeException extends Exception {
    private static final long serialVersionUID = 4246265102744292195L;

    public TradeException(String message, Object... args) {
        super(String.format(message, args));
    }

    public TradeException(String message) {
        super(message);
    }

    public TradeException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}