package com.vitaxa.jasteambot.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class ConcurrentHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentHelper.class);

    private ConcurrentHelper() {
    }

    public static void stop(ExecutorService executor) {
        stop(executor, 30, TimeUnit.SECONDS);
    }

    public static void stop(ExecutorService executor, long timeout, TimeUnit timeUnit) {
        try {
            executor.shutdown();
            executor.awaitTermination(timeout, timeUnit);
        } catch (InterruptedException e) {
            LOG.error("termination interrupted");
        } finally {
            if (!executor.isTerminated()) {
                LOG.error("killing non-finished tasks");
            }
            executor.shutdownNow();
        }
    }

    public static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            LOG.error("Thread sleep was interrupted", e);
        }
    }

    public static void sleepInMillis(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.error("Thread sleep was interrupted", e);
        }
    }

}
