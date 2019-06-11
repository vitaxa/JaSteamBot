package com.vitaxa.jasteambot.command.chat.basic;

import ch.qos.logback.classic.Level;
import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.command.chat.BotCommand;
import com.vitaxa.jasteambot.helper.CommonHelper;
import com.vitaxa.jasteambot.service.log.LogManager;
import uk.co.thomasc.steamkit.base.generated.enums.EChatEntryType;
import uk.co.thomasc.steamkit.types.SteamID;

public final class DebugCommand extends BotCommand {

    public DebugCommand(Bot bot, SteamID otherSID) {
        super(bot, otherSID);
    }

    @Override
    protected void execute(String... args) {
        final boolean b = CommonHelper.toBoolean(args[0]);
        LogManager.getInstance().setLoggingLevel(b ? Level.DEBUG : Level.INFO, bot.getBotConfig().getUsername());
        bot.getSteamFriends().sendChatMessage(otherSID, EChatEntryType.ChatMsg, "Debug enabled: " + b);
    }

    @Override
    protected boolean beforeExecuteCheck(String... args) {
        return args == null || args.length == 0;
    }

    @Override
    public String commandName() {
        return "debug";
    }

    @Override
    public String getArgsDescription() {
        return "[true/false]";
    }

    @Override
    public String getUsageDescription() {
        return "Debug log level";
    }
}
