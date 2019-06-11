package com.vitaxa.jasteambot;

import com.vitaxa.jasteambot.helper.ConcurrentHelper;
import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.callback.DotaMatchDetailCallback;
import uk.co.thomasc.steamkit.base.ClientGCMsgProtobuf;
import uk.co.thomasc.steamkit.base.ClientMsgProtobuf;
import uk.co.thomasc.steamkit.base.IClientGCMsg;
import uk.co.thomasc.steamkit.base.IPacketGCMsg;
import uk.co.thomasc.steamkit.base.generated.SteammessagesClientserver.CMsgClientGamesPlayed;
import uk.co.thomasc.steamkit.base.generated.enums.EMsg;
import uk.co.thomasc.steamkit.base.generated.enums.EResult;
import uk.co.thomasc.steamkit.base.generated.gc.dota.MsgGCClient.CMsgGCMatchDetailsRequest;
import uk.co.thomasc.steamkit.base.generated.gc.dota.MsgGCClient.CMsgGCMatchDetailsResponse;
import uk.co.thomasc.steamkit.base.generated.gc.dota.MsgGCMsgId;
import uk.co.thomasc.steamkit.base.generated.gc.dota.SteamMsgGCSystem;
import uk.co.thomasc.steamkit.steam3.handlers.steamgamecoordinator.callbacks.MessageCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.DisconnectedCallback;
import uk.co.thomasc.steamkit.types.AsyncJob;
import uk.co.thomasc.steamkit.types.SimpleAsyncJob;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static uk.co.thomasc.steamkit.base.generated.gc.dota.SteamMsgGCSDK.*;

public final class DotaBot extends Bot {

    private final Map<Integer, Consumer<IPacketGCMsg>> gcMessageHandlers = new HashMap<>();
    private volatile boolean canHandleGC = false;

    public DotaBot(BotConfig botConfig) {
        super(botConfig);
        initSubscribeCallBackMessages();
    }

    private void initSubscribeCallBackMessages() {
        gcMessageHandlers.put(SteamMsgGCSystem.EGCBaseClientMsg.k_EMsgGCClientWelcome.getNumber(), this::onClientWelcome);
        gcMessageHandlers.put(MsgGCMsgId.EDOTAGCMsg.k_EMsgGCMatchDetailsResponse.getNumber(), this::onMatchDetails);
        subscribeCallBackMessage(MessageCallback.class, callback -> {
            final Consumer<IPacketGCMsg> msgConsumer = gcMessageHandlers.get(callback.getEMsg());
            if (msgConsumer == null) {
                log.warn("Unknown callback msg: {}", callback.getEMsg());
                return; // Ignore
            }
            msgConsumer.accept(callback.getMessage());
        });
    }

    public AsyncJob<DotaMatchDetailCallback> getMatchDetail(long matchId) {
        final ClientGCMsgProtobuf<CMsgGCMatchDetailsRequest.Builder> requestMatch = new ClientGCMsgProtobuf<>(CMsgGCMatchDetailsRequest.class,
                MsgGCMsgId.EDOTAGCMsg.k_EMsgGCMatchDetailsRequest.getNumber());
        requestMatch.setSourceJobID(steamClient.getNextJobID());
        requestMatch.getBody().setMatchId(matchId);
        sendGCMsg(requestMatch);

        return new SimpleAsyncJob<DotaMatchDetailCallback>(steamClient, requestMatch.getSourceJobID());
    }

    @Override
    protected void onLoggedOn(LoggedOnCallback callback) {
        super.onLoggedOn(callback);

        if (callback.getResult() != EResult.OK) return;

        log.info("Logged in! Launching DOTA...");

        // we've logged into the account
        // now we need to inform the steam server that we're playing dota (in order to receive GC messages)

        // steamkit doesn't expose the "play game" message through any handler, so we'll just send the message manually
        final ClientMsgProtobuf<CMsgClientGamesPlayed.Builder> playGame = new ClientMsgProtobuf<>(CMsgClientGamesPlayed.class, EMsg.ClientGamesPlayed);
        playGame.getBody().addGamesPlayed(CMsgClientGamesPlayed.GamePlayed.newBuilder().setGameId(GameType.DOTA2.getAppid()));

        // send it off
        // notice here we're sending this message directly using the SteamClient
        steamClient.send(playGame);

        // delay a little to give steam some time to establish a GC connection to us
        ConcurrentHelper.sleep(5);

        // inform the dota GC that we want a session
        final ClientGCMsgProtobuf<CMsgClientHello.Builder> clientHello = new ClientGCMsgProtobuf<>(CMsgClientHello.class,
                SteamMsgGCSystem.EGCBaseClientMsg.k_EMsgGCClientHello.getNumber());
        clientHello.getBody().setEngine(ESourceEngine.k_ESE_Source2);
        steamGC.send(clientHello, Math.toIntExact(GameType.DOTA2.getAppid()));
    }

    @Override
    protected void onDisconnected(DisconnectedCallback callback) {
        super.onDisconnected(callback);
        canHandleGC = false;
    }

    private void onClientWelcome(IPacketGCMsg packetMsg) {
        final ClientGCMsgProtobuf<CMsgClientWelcome.Builder> msg = new ClientGCMsgProtobuf<>(CMsgClientWelcome.class, packetMsg);
        log.info("GC is welcoming us. Version: {}", msg.getBody().getVersion());

        // at this point, the GC is now ready to accept messages from us
        canHandleGC = true;
    }

    private void onMatchDetails(IPacketGCMsg packetMsg) {
        final ClientGCMsgProtobuf<CMsgGCMatchDetailsResponse.Builder> msg = new ClientGCMsgProtobuf<>(CMsgGCMatchDetailsResponse.class, packetMsg);

        final EResult result = EResult.from(msg.getBody().getResult());
        if (result != EResult.OK) {
            log.error("Unable to request match details: {}", result);
        }

        steamClient.postCallback(new DotaMatchDetailCallback(msg.getTargetJobID(), msg.getBody()));
    }

    private void sendGCMsg(IClientGCMsg clientGCMsg) {
        if (!canHandleGC) {
            throw new IllegalStateException("You need to wait until GC will be ready to accept messages from us");
        }
        steamGC.send(clientGCMsg, Math.toIntExact(GameType.DOTA2.getAppid()));
    }

}
