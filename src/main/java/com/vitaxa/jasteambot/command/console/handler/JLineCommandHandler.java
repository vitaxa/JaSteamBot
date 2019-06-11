package com.vitaxa.jasteambot.command.console.handler;

import com.vitaxa.jasteambot.JaSteamServer;
import jline.console.ConsoleReader;

import java.io.IOException;

public final class JLineCommandHandler extends CommandHandler {

    private final ConsoleReader reader;

    public JLineCommandHandler(JaSteamServer jasteam) throws IOException {
        super(jasteam);

        // Set reader
        reader = new ConsoleReader();
        reader.setExpandEvents(false);
    }

    @Override
    public void bell() throws IOException {
        reader.beep();
    }

    @Override
    public void clear() throws IOException {
        reader.clearScreen();
    }

    @Override
    public String readLine() throws IOException {
        return reader.readLine();
    }
}
