package com.vitaxa.jasteambot.steam.event;

@FunctionalInterface
public interface EventListener<T> {
    void invoke(T args);
}
