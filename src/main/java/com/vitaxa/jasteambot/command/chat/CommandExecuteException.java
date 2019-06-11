package com.vitaxa.jasteambot.command.chat;

public class CommandExecuteException extends Exception {

    private static final long serialVersionUID = 6104184714019123648L;

    public CommandExecuteException(String message) {
        super(message);
    }

    public CommandExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandExecuteException(Throwable cause) {
        super(cause);
    }
}
