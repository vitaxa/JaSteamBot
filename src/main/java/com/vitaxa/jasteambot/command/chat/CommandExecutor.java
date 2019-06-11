package com.vitaxa.jasteambot.command.chat;

@FunctionalInterface
public interface CommandExecutor {
    /**
     * Execute command
     *
     * @param command command name
     * @throws CommandExecuteException if something goes wrong
     */
    void executeCommand(String command) throws CommandExecuteException;
}
