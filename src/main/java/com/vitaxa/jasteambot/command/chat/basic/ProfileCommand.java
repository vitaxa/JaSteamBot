package com.vitaxa.jasteambot.command.chat.basic;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.command.chat.BotCommand;
import com.vitaxa.jasteambot.steam.web.model.SteamProfileInfo;
import uk.co.thomasc.steamkit.base.generated.enums.EChatEntryType;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.Optional;

public final class ProfileCommand extends BotCommand {

    public ProfileCommand(Bot bot, SteamID otherSID) {
        super(bot, otherSID);
    }

    @Override
    protected void execute(String... args) {
        final Optional<SteamProfileInfo> profileInfoOptional = bot.getSteamWebApi().getProfileInfo(
                String.valueOf(bot.getSteamClient().getSteamID().convertToUInt64()));
        if (profileInfoOptional.isPresent()) {
            final String msg = profileInfoToMessage(profileInfoOptional.get());
            bot.getSteamFriends().sendChatMessage(otherSID, EChatEntryType.ChatMsg, msg);
        }
    }

    @Override
    public String commandName() {
        return "profile";
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Bot is showing it's profile";
    }

    private String profileInfoToMessage(SteamProfileInfo profileInfo) {
        return System.lineSeparator() + "Profile Info:" + System.lineSeparator() +
                " |- SteamID:   " + profileInfo.getSteamId() + System.lineSeparator() +
                " |- Name:   " + profileInfo.getPersonaName() + System.lineSeparator() +
                " |- Avatar:        " + profileInfo.getAvatarFull() + System.lineSeparator() +
                " |- LastLogOff: " + profileInfo.getLastLogoff() + System.lineSeparator() +
                " |- TimeCreated:  " + profileInfo.getTimeCreated() + System.lineSeparator();
    }
}
