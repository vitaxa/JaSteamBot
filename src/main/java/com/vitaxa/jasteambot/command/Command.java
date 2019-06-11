package com.vitaxa.jasteambot.command;

public interface Command {

    public abstract void invoke(String... args) throws Exception;

    String getArgsDescription();

    String getUsageDescription();

}
