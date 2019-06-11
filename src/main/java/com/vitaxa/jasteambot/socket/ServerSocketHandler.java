package com.vitaxa.jasteambot.socket;

import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.helper.CommonHelper;
import com.vitaxa.jasteambot.socket.response.ResponseThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class ServerSocketHandler implements Runnable, AutoCloseable {

    private static final ThreadFactory THREAD_FACTORY = r -> CommonHelper.newThread("Network Thread", true, r);

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSocketHandler.class);

    // Instance
    private final JaSteamServer jaSteamServer;
    private final AtomicReference<ServerSocket> serverSocket = new AtomicReference<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool(THREAD_FACTORY);

    private final AtomicLong idCounter = new AtomicLong(0L);

    // Socket address
    private final SocketAddress address;

    public ServerSocketHandler(JaSteamServer jaSteamServer) {
        this.jaSteamServer = jaSteamServer;
        this.address = new InetSocketAddress(jaSteamServer.getConfig().getPort());
    }

    @Override
    public void close() {
        ServerSocket socket = serverSocket.getAndSet(null);
        if (socket != null) {
            LOGGER.info("Closing server socket listener");
            try {
                socket.close();
            } catch (IOException e) {
                LOGGER.error("Socket close exception", e);
            }
        }
    }

    @Override
    public void run() {
        LOGGER.info("Starting server socket thread");
        try (ServerSocket serverSocket = new ServerSocket()) {
            if (!this.serverSocket.compareAndSet(null, serverSocket)) {
                throw new IllegalStateException("Previous socket wasn't closed");
            }

            // Set socket params
            serverSocket.setReuseAddress(true);
            serverSocket.setPerformancePreferences(1, 0, 2);

            serverSocket.bind(address);
            LOGGER.info("Server socket thread successfully started");

            // Listen for incoming connections
            while (serverSocket.isBound()) {
                final Socket socket = serverSocket.accept();

                // Reply in separate thread
                threadPool.execute(new ResponseThread(jaSteamServer, idCounter.incrementAndGet(), socket));
            }
        } catch (IOException e) {
            // Ignore error after close/rebind
            if (serverSocket.get() != null) {
                LOGGER.error("Socket run exception", e);
            }
        }
    }
}


