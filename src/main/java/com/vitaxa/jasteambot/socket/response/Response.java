package com.vitaxa.jasteambot.socket.response;

import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;
import com.vitaxa.jasteambot.socket.RequestException;

import java.io.IOException;

public abstract class Response {

    protected final JaSteamServer jaSteamServer;
    protected final long id;
    protected final HInput input;
    protected final HOutput output;

    Response(JaSteamServer jaSteamServer, long id, HInput input, HOutput output) {
        this.jaSteamServer = jaSteamServer;
        this.id = id;
        this.input = input;
        this.output = output;
    }

    public static void requestError(String message) throws RequestException {
        throw new RequestException(message);
    }

    public abstract void reply() throws Exception;

    protected final void writeNoError(HOutput output) throws IOException {
        output.writeString("", 0);
    }


}
