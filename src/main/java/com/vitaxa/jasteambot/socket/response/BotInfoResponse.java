package com.vitaxa.jasteambot.socket.response;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;
import com.vitaxa.jasteambot.socket.response.model.BotInfo;
import com.vitaxa.steamauth.helper.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BotInfoResponse extends Response {

    BotInfoResponse(JaSteamServer jaSteamServer, long id, HInput input, HOutput output) {
        super(jaSteamServer, id, input, output);
    }

    @Override
    public void reply() throws Exception {
        final Map<Integer, Bot> runningBots = jaSteamServer.getBotManager().getRunningBots();
        final List<BotInfo> botInfoList = new ArrayList<>();
        jaSteamServer.getBotManager().getAllBots().forEach((integer, botConfig) -> {
            final Bot bot = runningBots.get(integer);
            if (bot != null) {
                botInfoList.add(new BotInfo(bot.getBotConfig().getDisplayName(), true, bot.getBotConfig().getType().getName()));
            }
        });
        final String result = Json.getInstance().toJson(botInfoList);

        writeNoError(output);

        output.writeString(result, 0);
    }
}
