package com.vitaxa.jasteambot.command.chat;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.command.Command;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class BotCommand implements Command {

    protected final Bot bot;

    protected final SteamID otherSID;

    private final List<BotCommand> childCommand = new ArrayList<>();

    public BotCommand(Bot bot, SteamID otherSID) {
        this.bot = bot;
        this.otherSID = otherSID;
    }

    @Override
    public final void invoke(String... args) throws Exception {
        // Check args if we need to call child command
        if (args != null && args.length > 0) {
            final Optional<BotCommand> botCommandOptional = childCommand.stream()
                    .filter(command -> command.commandName().equalsIgnoreCase(args[0]))
                    .findFirst();
            if (botCommandOptional.isPresent()) {
                botCommandOptional.get().invoke(args);
                return;
            }
        }
        if (beforeExecuteCheck(args)) {
            execute(args);
        }
    }

    protected void addChildCommand(BotCommand command) {
        childCommand.add(command);
    }

    protected abstract void execute(String... args);

    protected boolean beforeExecuteCheck(String... args) {
        return true;
    }

    public abstract String commandName();

    public Bot getBot() {
        return bot;
    }

    public SteamID getOtherSID() {
        return otherSID;
    }

    public List<BotCommand> getChildCommand() {
        return Collections.unmodifiableList(childCommand);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BotCommand that = (BotCommand) o;

        if (bot != null ? !bot.equals(that.bot) : that.bot != null) return false;
        return otherSID != null ? otherSID.equals(that.otherSID) : that.otherSID == null;
    }

    @Override
    public int hashCode() {
        int result = bot != null ? bot.hashCode() : 0;
        result = 31 * result + (otherSID != null ? otherSID.hashCode() : 0);
        return result;
    }
}

