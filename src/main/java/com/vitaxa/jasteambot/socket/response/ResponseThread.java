package com.vitaxa.jasteambot.socket.response;

import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.helper.IOHelper;
import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;
import com.vitaxa.jasteambot.socket.RequestException;
import com.vitaxa.jasteambot.socket.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public final class ResponseThread implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseThread.class);

    private final JaSteamServer jaSteamServer;

    private final long id;

    private final Socket socket;

    public ResponseThread(JaSteamServer jaSteamServer, long id, Socket socket) throws SocketException {
        this.jaSteamServer = jaSteamServer;
        this.id = id;
        this.socket = socket;

        // Set socket flags
        socket.setKeepAlive(false);
        socket.setTcpNoDelay(false);
        socket.setReuseAddress(true);

        // Set socket options
        socket.setSoTimeout(30000);
        socket.setTrafficClass(0b11100);
        socket.setPerformancePreferences(1, 0, 2);
    }

    @Override
    public void run() {
        boolean cancelled = false;

        LOG.debug("Connection {} from {}", id, IOHelper.getIP(socket.getRemoteSocketAddress()));

        // Process connection
        try (HInput input = new HInput(socket.getInputStream());
             HOutput output = new HOutput(socket.getOutputStream())) {

            RequestType type = readHandshake(input, output);

            if (type == null) { // Not accepted
                cancelled = true;
                return;
            }

            // Start response
            try {
                respond(type, input, output);
            } catch (RequestException e) {
                LOG.debug("{} Request error", id, e.getMessage());
                output.writeString(e.getMessage(), 0);
            }
        } catch (Exception e) {
            LOG.error("Failed to handleRequest connection", e);
        } finally {
            IOHelper.close(socket);
        }

    }

    private RequestType readHandshake(HInput input, HOutput output) throws IOException {
        // Read request type
        RequestType type = RequestType.read(input);

        // Protocol successfully verified
        output.writeBoolean(true);
        output.flush();
        return type;
    }

    private void respond(RequestType type, HInput input, HOutput output) throws Exception {
        LOG.info("Connection {} from {}: {}", id, IOHelper.getIP(socket.getRemoteSocketAddress()), type.name());

        // Choose response based on type
        Response response;
        switch (type) {
            case BOT_ALL_INFO:
                response = new BotInfoResponse(jaSteamServer, id, input, output);
                break;
            default:
                throw new AssertionError("Unsupported request type: " + type.name());
        }

        // Reply
        response.reply();
    }
}
