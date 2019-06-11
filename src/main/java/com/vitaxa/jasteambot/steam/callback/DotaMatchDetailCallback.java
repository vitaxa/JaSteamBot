package com.vitaxa.jasteambot.steam.callback;

import uk.co.thomasc.steamkit.base.generated.gc.dota.MsgGCClient;
import uk.co.thomasc.steamkit.base.generated.gc.dota.MsgGCCommon.CMsgDOTAMatch;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.types.JobID;

import static uk.co.thomasc.steamkit.base.generated.gc.dota.MsgSharedEnums.DOTAMatchVote;

public class DotaMatchDetailCallback extends CallbackMsg {

    private final int result;

    private final CMsgDOTAMatch match;

    private final DOTAMatchVote matchVote;

    public DotaMatchDetailCallback(JobID jobID, MsgGCClient.CMsgGCMatchDetailsResponse.Builder msg) {
        setJobID(jobID);

        this.result = msg.getResult();
        this.match = msg.getMatch();
        this.matchVote = msg.getVote();
    }

    public int getResult() {
        return result;
    }

    public CMsgDOTAMatch getMatch() {
        return match;
    }

    public DOTAMatchVote getMatchVote() {
        return matchVote;
    }

}
