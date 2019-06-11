package com.vitaxa.jasteambot;

import com.vitaxa.jasteambot.helper.CSGOSharedCodeHelper;
import com.vitaxa.jasteambot.helper.ConcurrentHelper;
import com.vitaxa.jasteambot.model.ShareCodeStruct;
import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.callback.CSGOMatchDetailCallback;
import uk.co.thomasc.steamkit.base.ClientGCMsgProtobuf;
import uk.co.thomasc.steamkit.base.ClientMsgProtobuf;
import uk.co.thomasc.steamkit.base.IClientGCMsg;
import uk.co.thomasc.steamkit.base.IPacketGCMsg;
import uk.co.thomasc.steamkit.base.generated.SteammessagesClientserver;
import uk.co.thomasc.steamkit.base.generated.enums.EMsg;
import uk.co.thomasc.steamkit.base.generated.enums.EResult;
import uk.co.thomasc.steamkit.base.generated.gc.csgo.SteamMsgGCSystem;
import uk.co.thomasc.steamkit.steam3.handlers.steamgamecoordinator.callbacks.MessageCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.DisconnectedCallback;
import uk.co.thomasc.steamkit.types.AsyncJob;
import uk.co.thomasc.steamkit.types.JobID;
import uk.co.thomasc.steamkit.types.SimpleAsyncJob;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import static uk.co.thomasc.steamkit.base.generated.gc.csgo.MsgGC.*;
import static uk.co.thomasc.steamkit.base.generated.gc.csgo.SteamMsgGCSDK.CMsgClientHello;
import static uk.co.thomasc.steamkit.base.generated.gc.csgo.SteamMsgGCSDK.CMsgClientWelcome;

public final class CSGOBot extends Bot {

    private final BlockingQueue<JobID> gcMessageQueue = new ArrayBlockingQueue<>(1);
    private final Map<Integer, Consumer<IPacketGCMsg>> gcMessageHandlers = new HashMap<>();
    private volatile boolean canHandleGC = false;

    public CSGOBot(BotConfig botConfig) {
        super(botConfig);
        initSubscribeCallBackMessages();
    }

    private void initSubscribeCallBackMessages() {
        gcMessageHandlers.put(SteamMsgGCSystem.EGCBaseClientMsg.k_EMsgGCClientWelcome.getNumber(), this::onClientWelcome);
        gcMessageHandlers.put(ECsgoGCMsg.k_EMsgGCCStrike15_v2_MatchList.getNumber(), this::onMatchDetail);
        subscribeCallBackMessage(MessageCallback.class, callback -> {
            final Consumer<IPacketGCMsg> msgConsumer = gcMessageHandlers.get(callback.getEMsg());
            if (msgConsumer == null) {
                log.warn("Unknown callback msg: {}", callback.getEMsg());
                return; // Ignore
            }
            msgConsumer.accept(callback.getMessage());
        });
    }

    @Override
    protected void onLoggedOn(LoggedOnCallback callback) {
        super.onLoggedOn(callback);

        if (callback.getResult() != EResult.OK) return;

        log.info("Logged in! Launching CSGO...");

        final ClientMsgProtobuf<SteammessagesClientserver.CMsgClientGamesPlayed.Builder> playGame =
                new ClientMsgProtobuf<>(SteammessagesClientserver.CMsgClientGamesPlayed.class, EMsg.ClientGamesPlayed);
        playGame.getBody().addGamesPlayed(SteammessagesClientserver.CMsgClientGamesPlayed.GamePlayed.newBuilder().setGameId(GameType.CSGO.getAppid()));

        steamClient.send(playGame);

        ConcurrentHelper.sleep(5);

        final ClientGCMsgProtobuf<CMsgClientHello.Builder> clientHello = new ClientGCMsgProtobuf<>(CMsgClientHello.class,
                SteamMsgGCSystem.EGCBaseClientMsg.k_EMsgGCClientHello.getNumber());
        steamGC.send(clientHello, Math.toIntExact(GameType.CSGO.getAppid()));
    }

    @Override
    protected void onDisconnected(DisconnectedCallback callback) {
        super.onDisconnected(callback);
        canHandleGC = false;
    }

    public AsyncJob<CSGOMatchDetailCallback> getMatchDetail(String sharedCode) {
        final ClientGCMsgProtobuf<CMsgGCCStrike15_v2_MatchListRequestFullGameInfo.Builder> requestMatch = new ClientGCMsgProtobuf<>(CMsgGCCStrike15_v2_MatchListRequestFullGameInfo.class,
                ECsgoGCMsg.k_EMsgGCCStrike15_v2_MatchListRequestFullGameInfo.getNumber());
        final ShareCodeStruct shareCodeStruct = CSGOSharedCodeHelper.decode(sharedCode);
        requestMatch.setSourceJobID(steamClient.getNextJobID());
        requestMatch.getBody().setMatchid(shareCodeStruct.getMatchId());
        requestMatch.getBody().setOutcomeid(shareCodeStruct.getOutcomeId());
        requestMatch.getBody().setToken(shareCodeStruct.getToken());

        try {
            gcMessageQueue.put(requestMatch.getSourceJobID());
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        sendGCMsg(requestMatch);

        return new SimpleAsyncJob<CSGOMatchDetailCallback>(steamClient, requestMatch.getSourceJobID());
    }

    public void onMatchDetail(IPacketGCMsg packetMsg) {
        try {
            final ClientGCMsgProtobuf<CMsgGCCStrike15_v2_MatchList.Builder> msg = new ClientGCMsgProtobuf<>(CMsgGCCStrike15_v2_MatchList.class, packetMsg);
            steamClient.postCallback(new CSGOMatchDetailCallback(gcMessageQueue.take(), msg.getBody()));
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void onClientWelcome(IPacketGCMsg packetMsg) {
        final ClientGCMsgProtobuf<CMsgClientWelcome.Builder> msg =
                new ClientGCMsgProtobuf<>(CMsgClientWelcome.class, packetMsg);
        log.info("GC is welcoming us. Version: {}", msg.getBody().getVersion());

        // at this point, the GC is now ready to accept messages from us
        canHandleGC = true;
    }

    private void sendGCMsg(IClientGCMsg clientGCMsg) {
        if (!canHandleGC) {
            throw new IllegalStateException("You need to wait until GC will be ready to accept messages from us");
        }
        steamGC.send(clientGCMsg, Math.toIntExact(GameType.CSGO.getAppid()));
    }


}
