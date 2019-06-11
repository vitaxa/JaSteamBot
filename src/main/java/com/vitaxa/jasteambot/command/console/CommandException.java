package com.vitaxa.jasteambot.command.console;

public final class CommandException extends Exception {
    private static final long serialVersionUID = -65888116482117772L;

    public CommandException(String message) {
        super(message);
    }

    public CommandException(Throwable exc) {
        super(exc);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
