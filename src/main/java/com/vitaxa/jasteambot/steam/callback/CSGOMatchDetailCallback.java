package com.vitaxa.jasteambot.steam.callback;

import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.types.JobID;

import java.util.List;

import static uk.co.thomasc.steamkit.base.generated.gc.csgo.MsgGC.*;

public class CSGOMatchDetailCallback extends CallbackMsg {

    private List<CDataGCCStrike15_v2_MatchInfo> matchList;

    private List<TournamentTeam> streamList;

    private CDataGCCStrike15_v2_TournamentInfo tournamentInfo;

    public CSGOMatchDetailCallback(JobID jobID, CMsgGCCStrike15_v2_MatchList.Builder msg) {
        setJobID(jobID);

        this.matchList = msg.getMatchesList();
        this.streamList = msg.getStreamsList();
        this.tournamentInfo = msg.getTournamentinfo();
    }

    public List<CDataGCCStrike15_v2_MatchInfo> getMatchList() {
        return matchList;
    }

    public List<TournamentTeam> getStreamList() {
        return streamList;
    }

    public CDataGCCStrike15_v2_TournamentInfo getTournamentInfo() {
        return tournamentInfo;
    }
}
