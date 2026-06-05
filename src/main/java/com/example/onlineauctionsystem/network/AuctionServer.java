package com.example.onlineauctionsystem.network;

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

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  AuctionServer  –  Server TCP cho Hệ thống Đấu giá Online
 * ╠══════════════════════════════════════════════════════════════╣
 *
 *  Cơ chế hoạt động:
 *  ┌─────────────────────────────────────────────────────────────┐
 *  │  1. main() mở ServerSocket lắng nghe tại PORT (mặc định 5000)
 *  │  2. Vòng lặp vô tận: serverSocket.accept() chờ client kết nối
 *  │  3. Mỗi kết nối mới → tạo 1 luồng worker xử lý độc lập
 *  │     (CachedThreadPool – tự động tạo/tái sử dụng thread)
 *  │  4. handleClient():
 *  │       a. Đọc AuctionMessage từ ObjectInputStream
 *  │       b. Gọi AuctionService.handleRequest() xử lý logic
 *  │       c. Ghi AuctionMessage phản hồi vào ObjectOutputStream
 *  │       d. Đóng socket
 *  │  5. ScheduledExecutorService chạy background mỗi 30 giây:
 *  │       → DataStorage.autoCloseAndSaveExpiredProducts()
 *  └─────────────────────────────────────────────────────────────┘
 *
 *  Cách chạy:
 *    Tùy chọn 1 – IntelliJ IDEA:
 *      Run → Edit Configurations → Add "Application"
 *      Main class: com.example.onlineauctionsystem.network.AuctionServer
 *      → Run
 *
 *    Tùy chọn 2 – Command line (sau khi build):
 *      java -cp target/classes:target/dependency/* \
 *           com.example.onlineauctionsystem.network.AuctionServer
 *
 *    Đổi port:
 *      java -Dauction.port=6000 -cp ... AuctionServer
 * ══════════════════════════════════════════════════════════════
 */
public final class AuctionServer {

    private static final int DEFAULT_PORT = 5000;

    // Không cho phép tạo instance từ bên ngoài
    private AuctionServer() {}

    // ──────────────────────────────────────────────────────────────
    //  ENTRY POINT
    // ──────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Báo cho DataStorage biết đang ở chế độ Server (không dùng local cache)
        System.setProperty("auction.remote", "false");

        int port = parsePort(System.getProperty("auction.port"), DEFAULT_PORT);

        // ── 1. Luồng bảo trì: tự động đóng phiên hết hạn ──
        ScheduledExecutorService maintenance = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "auction-maintenance");
            t.setDaemon(true); // Tự tắt khi main thread tắt
            return t;
        });
        maintenance.scheduleAtFixedRate(
                DataStorage::autoCloseAndSaveExpiredProducts,
                30,   // lần đầu sau 30 giây
                30,   // lặp lại mỗi 30 giây
                TimeUnit.SECONDS
        );
        System.out.println("[Server] Luồng bảo trì khởi động (30s/lần).");

        // ── 2. Thread pool xử lý client ──
        ExecutorService workers = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        // ── 3. Vòng lặp chính lắng nghe kết nối ──
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server] AuctionServer đang chạy tại port " + port);
            System.out.println("[Server] Nhấn Ctrl+C để dừng.\n");

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Chặn tại đây chờ client
                String clientAddr = clientSocket.getInetAddress().getHostAddress();
                System.out.println("[Server] Kết nối từ: " + clientAddr);

                // Mỗi client được xử lý trong thread riêng
                workers.submit(() -> handleClient(clientSocket));
            }
        } catch (Exception e) {
            System.err.println("[Server] Lỗi khởi động: " + e.getMessage());
        } finally {
            workers.shutdownNow();
            maintenance.shutdownNow();
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  XỬ LÝ MỘT CLIENT (chạy trong thread riêng)
    // ──────────────────────────────────────────────────────────────
    private static void handleClient(Socket socket) {
        String clientAddr = socket.getInetAddress().getHostAddress();
        try (
                socket; // try-with-resources: tự đóng socket khi xong
                ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            // Đọc yêu cầu từ client
            Object requestObject = in.readObject();

            AuctionMessage response;
            if (requestObject instanceof AuctionMessage request) {
                System.out.printf("[Server] ← [%s] Action: %s%n", clientAddr, request.getAction());
                // Giao cho AuctionService xử lý toàn bộ logic nghiệp vụ
                response = AuctionService.handleRequest(request);
            } else {
                response = new AuctionMessage(AuctionMessage.Action.ERROR, "Request không hợp lệ.");
            }

            // Gửi kết quả về client
            out.writeObject(response);
            out.flush();
            System.out.printf("[Server] → [%s] Response: %s%n", clientAddr, response.getAction());

        } catch (Exception e) {
            System.err.printf("[Server] Lỗi xử lý client [%s]: %s%n", clientAddr, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  TIỆN ÍCH
    // ──────────────────────────────────────────────────────────────
    private static int parsePort(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("[Server] Port không hợp lệ, dùng mặc định: " + fallback);
            return fallback;
        }
    }
}