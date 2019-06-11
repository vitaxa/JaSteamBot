package com.vitaxa.jasteambot.steam.event;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Event<T> {
    private final Set<EventListener<T>> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void addEventListener(EventListener<T> listener) {
        listeners.add(listener);
    }

    public void removeEventListener(EventListener<T> listener) {
        listeners.remove(listener);
    }

    public void handleEvent() {
        handleEvent(null);
    }

    public void handleEvent(T event) {
        for (EventListener<T> listener : listeners) {
            listener.invoke(event);
        }
    }

    /**
     * Remove all listeners from event
     */
    public void clear() {
        listeners.clear();
    }
}
