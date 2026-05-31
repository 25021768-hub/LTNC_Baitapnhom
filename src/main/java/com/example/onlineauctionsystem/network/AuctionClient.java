package com.example.onlineauctionsystem.network;

import com.example.onlineauctionsystem.model.AuctionMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public final class AuctionClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;

    private AuctionClient() {
    }

    public static AuctionMessage send(AuctionMessage request) {
        String host = System.getProperty("auction.host", DEFAULT_HOST);
        int port = parsePort(System.getProperty("auction.port"), DEFAULT_PORT);

        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            if (response instanceof AuctionMessage) {
                return (AuctionMessage) response;
            }
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Server trả về dữ liệu không hợp lệ.");
        } catch (Exception e) {
            return new AuctionMessage(
                    AuctionMessage.Action.ERROR,
                    "Không kết nối được server " + host + ":" + port + " - " + e.getMessage()
            );
        }
    }

    private static int parsePort(String value, int fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
