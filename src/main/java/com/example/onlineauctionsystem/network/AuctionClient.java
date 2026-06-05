package com.example.onlineauctionsystem.network;

import com.example.onlineauctionsystem.model.Account;
import com.example.onlineauctionsystem.model.BidHistory;
import com.example.onlineauctionsystem.model.Product;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  AuctionClient  –  Client TCP cho Hệ thống Đấu giá Online
 * ╠══════════════════════════════════════════════════════════════╣
 *
 *  Cơ chế hoạt động:
 *  ┌─────────────────────────────────────────────────────────────┐
 *  │  Mỗi lần gọi send(request):
 *  │    1. Mở Socket mới đến HOST:PORT
 *  │    2. Ghi AuctionMessage (request) vào ObjectOutputStream
 *  │    3. Đọc AuctionMessage (response) từ ObjectInputStream
 *  │    4. Đóng Socket
 *  │    → Thiết kế "connect-per-request": đơn giản, không cần
 *  │      quản lý trạng thái kết nối, phù hợp với JavaFX app.
 *  └─────────────────────────────────────────────────────────────┘
 *
 *  Cách cấu hình host/port:
 *    - Mặc định: localhost:5000
 *    - Đổi qua System Property khi chạy:
 *        java -Dauction.host=192.168.1.5 -Dauction.port=6000 -cp ... Launcher
 *
 *  Cách dùng từ Controller:
 *    // Gửi yêu cầu đăng nhập
 *    AuctionMessage req = new AuctionMessage(
 *        AuctionMessage.Action.LOGIN,
 *        new String[]{"user1", "pass123"}
 *    );
 *    AuctionMessage res = AuctionClient.send(req);
 *    if (res.getAction() == AuctionMessage.Action.SUCCESS) {
 *        Account acc = (Account) res.getData();
 *    }
 *
 *  main() — Dùng để TEST từ terminal (không cần JavaFX):
 *    java -cp ... com.example.onlineauctionsystem.network.AuctionClient
 * ══════════════════════════════════════════════════════════════
 */
public final class AuctionClient {

    private static final String DEFAULT_HOST = "localhost";
    private static final int    DEFAULT_PORT = 5000;

    // Không cho phép tạo instance
    private AuctionClient() {}

    // ──────────────────────────────────────────────────────────────
    //  PHƯƠNG THỨC CHÍNH: GỬI YÊU CẦU → NHẬN PHẢN HỒI
    // ──────────────────────────────────────────────────────────────
    /**
     * Gửi một AuctionMessage đến Server và trả về phản hồi.
     * Nếu có lỗi kết nối hoặc bất kỳ Exception nào, trả về
     * AuctionMessage với Action.ERROR và thông báo lỗi.
     *
     * @param request Yêu cầu cần gửi
     * @return Phản hồi từ Server (không bao giờ trả về null)
     */
    public static AuctionMessage send(AuctionMessage request) {
        String host = System.getProperty("auction.host", DEFAULT_HOST);
        int    port = parsePort(System.getProperty("auction.port"), DEFAULT_PORT);

        try (
                Socket socket = new Socket(host, port);
                // QUAN TRỌNG: ObjectOutputStream phải tạo TRƯỚC ObjectInputStream
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())
        ) {
            // Gửi yêu cầu
            out.writeObject(request);
            out.flush();

            // Nhận phản hồi
            Object response = in.readObject();
            if (response instanceof AuctionMessage msg) {
                return msg;
            }
            return new AuctionMessage(AuctionMessage.Action.ERROR,
                    "Server trả về dữ liệu không hợp lệ.");

        } catch (java.net.ConnectException e) {
            return new AuctionMessage(AuctionMessage.Action.ERROR,
                    "Không thể kết nối Server tại " + host + ":" + port
                            + ". Hãy đảm bảo AuctionServer đang chạy.");
        } catch (Exception e) {
            return new AuctionMessage(AuctionMessage.Action.ERROR,
                    "Lỗi mạng: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  PHƯƠNG THỨC TIỆN ÍCH (Wrapper cho từng action)
    //  Controller gọi các hàm này thay vì tự tạo AuctionMessage
    // ──────────────────────────────────────────────────────────────

    /** Đăng nhập. Trả về Account nếu thành công, null nếu thất bại. */
    public static Account login(String username, String password) {
        AuctionMessage res = send(new AuctionMessage(
                AuctionMessage.Action.LOGIN,
                new String[]{username, password}
        ));
        if (res.getAction() == AuctionMessage.Action.SUCCESS) {
            return (Account) res.getData();
        }
        return null;
    }

    /** Đăng ký tài khoản mới. Trả về thông báo lỗi hoặc null nếu thành công. */
    public static String register(Account acc) {
        AuctionMessage res = send(new AuctionMessage(AuctionMessage.Action.REGISTER, acc));
        if (res.getAction() == AuctionMessage.Action.SUCCESS) return null;
        return (String) res.getData();
    }

    /** Lấy danh sách tất cả sản phẩm. */
    @SuppressWarnings("unchecked")
    public static List<Product> getAllProducts() {
        AuctionMessage res = send(new AuctionMessage(AuctionMessage.Action.GET_ALL_PRODUCTS, null));
        if (res.getAction() == AuctionMessage.Action.SUCCESS) {
            return (List<Product>) res.getData();
        }
        return List.of();
    }

    /** Đặt giá cho sản phẩm. Trả về Product cập nhật mới nhất nếu thành công. */
    public static AuctionMessage placeBid(String productId, double amount, String bidderUsername) {
        return send(new AuctionMessage(
                AuctionMessage.Action.BID,
                new Object[]{productId, amount, bidderUsername}
        ));
    }

    /** Lấy lịch sử đặt giá của người dùng. */
    @SuppressWarnings("unchecked")
    public static List<BidHistory> getBidHistory(String username) {
        AuctionMessage res = send(new AuctionMessage(AuctionMessage.Action.GET_BID_HISTORY, username));
        if (res.getAction() == AuctionMessage.Action.SUCCESS) {
            return (List<BidHistory>) res.getData();
        }
        return List.of();
    }

    /** Đổi mật khẩu. Trả về null nếu thành công, thông báo lỗi nếu thất bại. */
    public static String changePassword(String username, String oldPass, String newPass) {
        AuctionMessage res = send(new AuctionMessage(
                AuctionMessage.Action.CHANGE_PASSWORD,
                new String[]{username, oldPass, newPass}
        ));
        if (res.getAction() == AuctionMessage.Action.SUCCESS) return null;
        return (String) res.getData();
    }

    /** Lấy số dư tài khoản. */
    public static double getBalance(String username) {
        AuctionMessage res = send(new AuctionMessage(AuctionMessage.Action.GET_BALANCE, username));
        if (res.getAction() == AuctionMessage.Action.SUCCESS) {
            return ((Number) res.getData()).doubleValue();
        }
        return 0.0;
    }

    // ──────────────────────────────────────────────────────────────
    //  MAIN - TEST CLIENT TỪ TERMINAL
    // ──────────────────────────────────────────────────────────────
    /**
     * Dùng để kiểm tra kết nối và các chức năng cơ bản từ terminal.
     * Chạy: java -cp ... com.example.onlineauctionsystem.network.AuctionClient
     *
     * Yêu cầu: AuctionServer phải đang chạy trước.
     */
    public static void main(String[] args) {
        System.out.println("=== AuctionClient Test Console ===");
        System.out.println("Server: " +
                System.getProperty("auction.host", DEFAULT_HOST) + ":" +
                parsePort(System.getProperty("auction.port"), DEFAULT_PORT));
        System.out.println("Gõ 'quit' để thoát.\n");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("--- MENU ---");
            System.out.println("1. Test kết nối (lấy danh sách sản phẩm)");
            System.out.println("2. Đăng nhập");
            System.out.println("3. Đặt giá thủ công");
            System.out.print("Chọn: ");

            String choice = scanner.nextLine().trim();
            if ("quit".equalsIgnoreCase(choice)) break;

            switch (choice) {
                case "1" -> {
                    System.out.println("[Test] Đang lấy danh sách sản phẩm...");
                    List<Product> products = getAllProducts();
                    if (products.isEmpty()) {
                        System.out.println("[Test] Không có sản phẩm hoặc lỗi kết nối.");
                    } else {
                        System.out.println("[Test] Tìm thấy " + products.size() + " sản phẩm:");
                        products.forEach(p -> System.out.printf(
                                "  - [%s] %s | Giá: %.0f | Trạng thái: %s%n",
                                p.getId(), p.getName(), p.getCurrentPrice(), p.getStatus()
                        ));
                    }
                }
                case "2" -> {
                    System.out.print("Username: ");
                    String user = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String pass = scanner.nextLine().trim();

                    Account acc = login(user, pass);
                    if (acc != null) {
                        System.out.printf("[Test] Đăng nhập thành công! Role: %s | Số dư: %.0f%n",
                                acc.getRole(), acc.getBalance());
                    } else {
                        System.out.println("[Test] Đăng nhập thất bại.");
                    }
                }
                case "3" -> {
                    System.out.print("Product ID: ");
                    String pid = scanner.nextLine().trim();
                    System.out.print("Số tiền đặt: ");
                    double amount;
                    try { amount = Double.parseDouble(scanner.nextLine().trim()); }
                    catch (NumberFormatException e) { System.out.println("Số không hợp lệ."); break; }
                    System.out.print("Username: ");
                    String bidder = scanner.nextLine().trim();

                    AuctionMessage res = placeBid(pid, amount, bidder);
                    System.out.println("[Test] Kết quả: " + res.getAction() + " | " + res.getData());
                }
                default -> System.out.println("Lựa chọn không hợp lệ.");
            }
            System.out.println();
        }

        scanner.close();
        System.out.println("Đã thoát AuctionClient Test.");
    }

    // ──────────────────────────────────────────────────────────────
    //  TIỆN ÍCH
    // ──────────────────────────────────────────────────────────────
    private static int parsePort(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

}