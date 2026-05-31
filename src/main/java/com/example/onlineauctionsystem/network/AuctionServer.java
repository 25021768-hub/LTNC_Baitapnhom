package com.example.onlineauctionsystem.network;

import com.example.onlineauctionsystem.model.AuctionMessage;
import com.example.onlineauctionsystem.model.AuctionService;
import com.example.onlineauctionsystem.model.DataStorage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AuctionServer {
    private static final int DEFAULT_PORT = 5000;

    private AuctionServer() {
    }

    public static void main(String[] args) {
        System.setProperty("auction.remote", "false");
        int port = parsePort(System.getProperty("auction.port"), DEFAULT_PORT);

        ScheduledExecutorService maintenance = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "auction-maintenance");
            t.setDaemon(true);
            return t;
        });
        maintenance.scheduleAtFixedRate(DataStorage::autoCloseAndSaveExpiredProducts, 30, 30, TimeUnit.SECONDS);

        ExecutorService workers = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("AuctionServer đang chạy tại port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                workers.submit(() -> handleClient(clientSocket));
            }
        } catch (Exception e) {
            System.err.println("Không khởi động được AuctionServer: " + e.getMessage());
        } finally {
            workers.shutdownNow();
            maintenance.shutdownNow();
        }
    }

    private static void handleClient(Socket socket) {
        try (socket;
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            Object requestObject = in.readObject();
            AuctionMessage response;
            if (requestObject instanceof AuctionMessage) {
                response = AuctionService.handleRequest((AuctionMessage) requestObject);
            } else {
                response = new AuctionMessage(AuctionMessage.Action.ERROR, "Request không hợp lệ.");
            }

            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            System.err.println("Lỗi xử lý client: " + e.getMessage());
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
