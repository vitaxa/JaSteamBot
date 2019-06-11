package com.vitaxa.jasteambot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.thomasc.steamkit.util.logging.IDebugListener;

public class SteamKitDebugListener implements IDebugListener {

    private static final Logger LOG = LoggerFactory.getLogger(SteamKitDebugListener.class);

    @Override
    public void writeLine(String s, String s1) {
        LOG.debug("[SteamKit] Category - {}. Message: {}", s, s1);
    }
}
