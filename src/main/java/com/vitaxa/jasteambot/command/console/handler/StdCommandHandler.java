package com.vitaxa.jasteambot.command.console.handler;

import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.helper.IOHelper;

import java.io.BufferedReader;
import java.io.IOException;

public final class StdCommandHandler extends CommandHandler {
    private final BufferedReader reader;

    public StdCommandHandler(JaSteamServer jasteam) {
        super(jasteam);
        reader = IOHelper.newReader(System.in);
    }

    @Override
    public void bell() {
        // Do nothing, unsupported
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear terminal");
    }

    @Override
    public String readLine() throws IOException {
        return reader.readLine();
    }
}
