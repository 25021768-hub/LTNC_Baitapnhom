package com.example.onlineauctionsystem.model;

import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.Validator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.chart.XYChart;

public class DataStorage {
    private static final String URL = "jdbc:mysql://localhost:3306/online_auction";
    private static final String USER = "root";
    private static final String PASS = "";
    public static Account currentAccount;
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static final Map<String, String> activeSessions = new ConcurrentHashMap<>();

    public static String createSession(String username) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(username, token);
        return token;
    }

    public static boolean isSessionValid(String username, String token) {
        return token != null && token.equals(activeSessions.get(username));
    }

    public static void removeSession(String username) {
        activeSessions.remove(username);
    }

    // --- QUẢN LÝ TÀI KHOẢN ---
    public static Account checkLogin(String username, String password) {
        String sql = "SELECT * FROM accounts WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Account acc = new Account();
                acc.setUsername(rs.getString("username"));
                acc.setRole(rs.getString("role"));
                acc.setIdCard(rs.getString("id_card"));
                acc.setEmail(rs.getString("email"));
                acc.setPhoneNumber(rs.getString("phone_number")); // Đúng tên phone_number
                acc.setBalance(rs.getDouble("balance"));
                acc.setPassword(rs.getString("password"));
                acc.setFullName(rs.getString("fullname"));
                acc.setLocked(rs.getBoolean("is_locked"));
                return acc;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static Account findAccountByUsername(String username) {
        String sql = "SELECT * FROM accounts WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Account acc = new Account();
                acc.setUsername(rs.getString("username"));
                acc.setPassword(rs.getString("password"));
                acc.setRole(rs.getString("role"));
                acc.setFullName(rs.getString("fullname"));
                acc.setIdCard(rs.getString("id_card"));
                acc.setEmail(rs.getString("email"));
                acc.setPhoneNumber(rs.getString("phone_number"));
                acc.setLocked(rs.getBoolean("is_locked"));
                return acc;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Check tài khoản tồn tài chưa
    public static boolean isAccountExists(String identifier){
        if(identifier == null || identifier.trim().isEmpty()){
            return false;
        }
        String sql = "SELECT COUNT(*) FROM accounts WHERE username = ? OR email = ? OR phone_number = ? OR id_card = ?";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);){
            String value = identifier.trim();
            stmt.setString(1, value);
            stmt.setString(2, value);
            stmt.setString(3, value);
            stmt.setString(4, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        }
        catch (SQLException e) {
            System.err.println("Lỗi truy vấn Database: " + e.getMessage());
        }
        return false;
    }

    //Đăng kí tài khoản
    public static boolean register(Account acc) {

        String sql = "INSERT INTO accounts (username, password, role, fullname, id_card, email, phone_number, is_locked) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, acc.getUsername());
            stmt.setString(2, acc.getPassword());
            stmt.setString(3, acc.getRole());
            stmt.setString(4, acc.getFullName());
            stmt.setString(5, acc.getIdCard());
            stmt.setString(6, acc.getEmail());
            stmt.setString(7, acc.getPhoneNumber());
            stmt.setBoolean(8,acc.isLocked());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Trả về false nếu trùng username hoặc lỗi kết nối
        }
    }

    //Đổi mật khẩu khi quên tên đăng nhập
    public static boolean changeForgotPassword(String identifier, String newPass){
        String sql = "UPDATE accounts SET password = ? WHERE phone_number = ? OR email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPass);
            stmt.setString(2, identifier);
            stmt.setString(3, identifier);
            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e){
            return false;
        }
    }

    public static boolean updateAccount(Account acc){
        // Chỉ SET những thứ cần thay đổi, tuyệt đối không SET password ở đây
        String sql = "UPDATE accounts SET fullname = ?, id_card = ?, email = ?, phone_number = ? WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, acc.getFullName());
            stmt.setString(2, acc.getIdCard());     // Khớp với id_card
            stmt.setString(3, acc.getEmail());      // Khớp với email
            stmt.setString(4, acc.getPhoneNumber());// Khớp với phone_number
            stmt.setString(5, acc.getUsername());   // Điều kiện để biết sửa ai

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Đổi mật khẩu khi biết tên đăng nhập
    public static boolean changePassword(String username, String oldPass, String newPass) {
        String sql = "UPDATE accounts SET password = ? WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPass);
            stmt.setString(2, username);
            stmt.setString(3, oldPass);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // --- QUẢN LÝ SẢN PHẨM ---
    public static List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product p = new Product(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDouble("initial_price"),
                        rs.getDouble("bid_increment"),
                        rs.getLong("duration_hours"),
                        rs.getString("seller_name"),
                        rs.getString("image_path")
                );
                p.setCurrentPrice(rs.getDouble("current_price"));
                p.setHighestBidder(rs.getString("highest_bidder"));
                p.setStatus(rs.getString("status"));

                // Đọc start_time
                Timestamp startTs = rs.getTimestamp("start_time");
                if (startTs != null) {
                    p.setStartTime(startTs.toLocalDateTime());
                }

                // ── BỔ SUNG: Đọc thêm cột end_time từ MySQL lên Java ──
                Timestamp endTs = rs.getTimestamp("end_time");
                if (endTs != null) {
                    p.setEndTime(endTs.toLocalDateTime());
                }

                p.updateStatus();
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean addProduct(Product p) {
        String sql = "INSERT INTO products (id, name, initial_price, bid_increment, " +
                "current_price, seller_name, image_path, highest_bidder, " +
                "duration_hours, start_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getId());
            stmt.setString(2, p.getName());
            stmt.setDouble(3, p.getInitialPrice());
            stmt.setDouble(4, p.getBidIncrement());
            stmt.setDouble(5, p.getCurrentPrice());
            stmt.setString(6, p.getSellerName());
            stmt.setString(7, p.getImagePath());
            stmt.setString(8, p.getHighestBidder());
            stmt.setLong(9, p.getDurationHours());

            // start_time NULL vì chờ admin duyệt
            if (p.getStartTime() != null) {
                stmt.setTimestamp(10, Timestamp.valueOf(p.getStartTime()));
            } else {
                stmt.setNull(10, Types.TIMESTAMP);
            }

            stmt.setString(11, p.getStatus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateBid(String productId, double newPrice, String bidderName) {
        // Chỉ update nếu newPrice > current_price hiện tại trong DB
        String sql = "UPDATE products SET current_price = ?, highest_bidder = ? " +
                "WHERE id = ? AND status = 'RUNNING' AND current_price < ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newPrice);
            stmt.setString(2, bidderName);
            stmt.setString(3, productId);
            stmt.setDouble(4, newPrice); // ← điều kiện: giá hiện tại < giá mới
            int rows = stmt.executeUpdate();

            if (rows > 0) {

                //  Kéo dài thời gian nếu còn < 5 phút
                extendIfLastMinutes(conn, productId);

                // 0. Ghi log thời gian thực hiện để phục vụ vẽ biểu đồ
                String logSql = "INSERT INTO product_price_log (product_id, bidder_name, price_milestone) VALUES (?, ?, ?)";
                try (PreparedStatement logStmt = conn.prepareStatement(logSql)) {
                    logStmt.setString(1, productId);
                    logStmt.setString(2, bidderName);
                    logStmt.setDouble(3, newPrice);
                    logStmt.executeUpdate();
                }

                // 1. Tìm sản phẩm trong DB để lấy bước giá (bid_increment) chuẩn của nó
                Product p = findProductById(productId);
                if (p != null) {
                    // 2. Kích hoạt chuỗi tự động nâng giá của hệ thống Auto Bid
                    triggerAutoBidSystem(productId, newPrice, p.getBidIncrement());
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // 1. Hàm tìm một sản phẩm cụ thể theo ID
    public static Product findProductById(String id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Product p = new Product(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDouble("initial_price"),
                        rs.getDouble("bid_increment"),
                        rs.getLong("duration_hours"),
                        rs.getString("seller_name"),
                        rs.getString("image_path")
                );
                p.setCurrentPrice(rs.getDouble("current_price"));
                p.setHighestBidder(rs.getString("highest_bidder"));
                p.setStatus(rs.getString("status"));

                // Đọc start_time
                Timestamp startTs = rs.getTimestamp("start_time");
                if (startTs != null) {
                    p.setStartTime(startTs.toLocalDateTime());
                }

                // ── BỔ SUNG: Đọc thêm cột end_time từ MySQL lên Java ──
                Timestamp endTs = rs.getTimestamp("end_time");
                if (endTs != null) {
                    p.setEndTime(endTs.toLocalDateTime());
                }

                p.updateStatus();
                return p;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Lấy lịch sử đấu giá của một bidder (sản phẩm đã kết thúc)
    public static List<BidHistory> getBidHistory(String bidderName) {
        List<BidHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM bid_history WHERE bidder_name = ? ORDER BY end_time DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bidderName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BidHistory h = new BidHistory();
                h.setProductId(rs.getString("product_id"));
                h.setProductName(rs.getString("product_name"));
                h.setMyBidPrice(rs.getDouble("my_bid_price"));
                h.setFinalPrice(rs.getDouble("final_price"));
                Timestamp ts = rs.getTimestamp("end_time");
                if (ts != null) h.setEndTime(ts.toLocalDateTime());
                h.setResult(rs.getString("result"));
                h.setPaid(rs.getBoolean("is_paid"));
                list.add(h);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Lấy sản phẩm đang đấu giá (RUNNING) mà bidder đang tham gia
    public static List<Product> getRunningBidsByBidder(String bidderName) {
        List<Product> list = new ArrayList<>();
        // Lấy các sản phẩm RUNNING mà bidder đã từng đặt giá (có tên trong bid_history hoặc là highest_bidder)
        String sql = "SELECT DISTINCT p.* FROM products p " +
                "LEFT JOIN bid_history bh ON p.id = bh.product_id " +
                "WHERE p.status = 'RUNNING' " +
                "AND (p.highest_bidder = ? OR bh.bidder_name = ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bidderName);
            stmt.setString(2, bidderName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product p = new Product(
                        rs.getString("id"), rs.getString("name"),
                        rs.getDouble("initial_price"), rs.getDouble("bid_increment"),
                        rs.getLong("duration_hours"), rs.getString("seller_name"),
                        rs.getString("image_path")
                );
                p.setCurrentPrice(rs.getDouble("current_price"));
                p.setHighestBidder(rs.getString("highest_bidder"));
                p.setStatus(rs.getString("status"));
                Timestamp startTs = rs.getTimestamp("start_time");
                if (startTs != null) p.setStartTime(startTs.toLocalDateTime());
                Timestamp endTs = rs.getTimestamp("end_time");
                if (endTs != null) p.setEndTime(endTs.toLocalDateTime());
                p.updateStatus();
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Ghi lịch sử khi phiên đấu giá kết thúc
    public static boolean saveBidHistory(BidHistory bidH) {
        String sql = """
        INSERT INTO bid_history 
            (bidder_name, product_id, product_name, my_bid_price, final_price, end_time, result, is_paid)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            my_bid_price = GREATEST(my_bid_price, VALUES(my_bid_price)),
            final_price  = VALUES(final_price),
            end_time     = VALUES(end_time),
            result       = VALUES(result)
        """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bidH.getBidderName());
            stmt.setString(2, bidH.getProductId());
            stmt.setString(3, bidH.getProductName());
            stmt.setDouble(4, bidH.getMyBidPrice());
            stmt.setDouble(5, bidH.getFinalPrice());

            if (bidH.getEndTime() != null) {
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(bidH.getEndTime()));
            } else {
                stmt.setNull(6, java.sql.Types.TIMESTAMP);
            }

            stmt.setString(7, bidH.getResult());
            stmt.setInt(8, bidH.getPaid() ? 1 : 0);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static double getMaxBidByBidderForProduct(String bidderName, String productId, double defaultPrice) {
        String sql = "SELECT MAX(my_bid_price) FROM bid_history WHERE bidder_name = ? AND product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bidderName);
            stmt.setString(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double maxBid = rs.getDouble(1);
                    if (maxBid > 0) {
                        return maxBid;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultPrice;
    }

    // Seller tự xóa SP của mình — chỉ được xóa khi PENDING
    public static boolean deleteMyProduct(String productId, String sellerUsername) {
        String sql = "DELETE FROM products WHERE id = ? AND seller_name = ? AND status = 'PENDING'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            stmt.setString(2, sellerUsername);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.err.println("Không thể xóa: SP không tồn tại, không phải của bạn, hoặc không ở trạng thái PENDING");
            }
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. Hàm xóa sản phẩm khỏi Database(chỉ cho ADMIN)
    // Hàm xóa sản phẩm (Chỉ Admin mới được xóa)
    public static boolean deleteProduct(String id, Account loggedInUser) {
        if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
            System.err.println("Từ chối truy cập: Chỉ ADMIN mới có quyền xóa sản phẩm!");
            return false;
        }

        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Lấy tất cả tài khoản
    public static List<Account> getAllAccounts() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Account acc = new Account();
                acc.setUsername(rs.getString("username"));
                acc.setFullName(rs.getString("fullname"));
                acc.setEmail(rs.getString("email"));
                acc.setPhoneNumber(rs.getString("phone_number"));
                acc.setIdCard(rs.getString("id_card"));
                acc.setRole(rs.getString("role"));
                acc.setLocked(rs.getBoolean("is_locked")); // cần thêm cột này
                list.add(acc);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Khóa / Mở tài khoản
    public static boolean setAccountLocked(String username, boolean locked) {
        String sql = "UPDATE accounts SET is_locked = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, locked);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateProductStatus(String productId, String newStatus) {
        // Nếu duyệt thành công (RUNNING), ta đồng thời kích hoạt luôn giờ bắt đầu đấu giá (NOW())
        String sql;
        if ("RUNNING".equals(newStatus)) {
            sql = "UPDATE products SET status = ?, start_time = NOW(), end_time = DATE_ADD(NOW(), INTERVAL duration_hours HOUR) WHERE id = ?";
        } else {
            sql = "UPDATE products SET status = ? WHERE id = ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setString(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy tên seller theo productId
    public static String getSellerByProductId(String productId) {
        String sql = "SELECT seller_name FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("seller_name");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Đánh dấu đã thanh toán trong bid_history và products
    public static boolean markAsPaid(String productId, String bidderName) {
        String sql1 = "UPDATE bid_history SET is_paid = true " +
                "WHERE product_id = ? AND bidder_name = ? AND result = 'WIN'";
        String sql2 = "UPDATE products SET status = 'PAID' WHERE id = ?";
        try (Connection conn = getConnection()) {
            try (PreparedStatement s1 = conn.prepareStatement(sql1)) {
                s1.setString(1, productId);
                s1.setString(2, bidderName);
                s1.executeUpdate();
            }
            try (PreparedStatement s2 = conn.prepareStatement(sql2)) {
                s2.setString(1, productId);
                s2.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy số dư hiện tại của tài khoản
    public static double getBalance(String username) {
        String sql = "SELECT balance FROM accounts WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    //Update status tài khoản (cộng/trừ tiền trong ví)
    public static boolean updateBalance(String username, double amountToChange) {
        // có thể là số âm (trừ tiền) hoặc số dương (nạp tiền/hoàn tiền)
        String sql = "UPDATE accounts SET balance = balance + ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amountToChange);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void autoCloseAndSaveExpiredProducts() {
        // Query 1: Chỉ lấy sản phẩm chưa có history để lưu
        String findExpiredSql = "SELECT id, name, current_price, highest_bidder FROM products " +
                "WHERE end_time <= NOW() AND status = 'RUNNING' " +
                "AND id NOT IN (SELECT DISTINCT product_id FROM bid_history)";

        String insertHistorySql = "INSERT INTO bid_history (bidder_name, product_id, product_name, my_bid_price, final_price, end_time, result, is_paid) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), ?, 0)";

        String findAllBiddersSql = "SELECT DISTINCT bidder_name, MAX(bid_amount) as max_bid FROM bids " +
                "WHERE product_id = ? AND bidder_name != ? GROUP BY bidder_name";

        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(findExpiredSql);
             ResultSet rs = selectStmt.executeQuery()) {

            while (rs.next()) {
                String productId = rs.getString("id");
                String productName = rs.getString("name");
                double finalPrice = rs.getDouble("current_price");
                String winner = rs.getString("highest_bidder");

                if (winner != null && !winner.trim().isEmpty() && !"None".equalsIgnoreCase(winner)) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertHistorySql)) {
                        insertStmt.setString(1, winner);
                        insertStmt.setString(2, productId);
                        insertStmt.setString(3, productName);
                        insertStmt.setDouble(4, finalPrice);
                        insertStmt.setDouble(5, finalPrice);
                        insertStmt.setString(6, "WIN");
                        insertStmt.executeUpdate();
                    }

                    try (PreparedStatement bidderStmt = conn.prepareStatement(findAllBiddersSql)) {
                        bidderStmt.setString(1, productId);
                        bidderStmt.setString(2, winner);
                        try (ResultSet rsBidders = bidderStmt.executeQuery()) {
                            while (rsBidders.next()) {
                                String loserName = rsBidders.getString("bidder_name");
                                double loserMaxBid = rsBidders.getDouble("max_bid");
                                try (PreparedStatement insertLooseStmt = conn.prepareStatement(insertHistorySql)) {
                                    insertLooseStmt.setString(1, loserName);
                                    insertLooseStmt.setString(2, productId);
                                    insertLooseStmt.setString(3, productName);
                                    insertLooseStmt.setDouble(4, loserMaxBid);
                                    insertLooseStmt.setDouble(5, finalPrice);
                                    insertLooseStmt.setString(6, "LOSE");
                                    insertLooseStmt.executeUpdate();
                                }
                            }
                        }
                    }
                    System.out.println("[HỆ THỐNG] Đã chốt phiên: " + productId);
                } else {
                    System.out.println("[HỆ THỐNG] Phiên không có người mua: " + productId);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi lưu lịch sử: " + e.getMessage());
            e.printStackTrace();
        }

        // ✅ BƯỚC 2: Đổi FINISHED riêng — bắt hết mọi sản phẩm hết hạn còn RUNNING
        // Tách ra dùng connection mới để tránh xung đột với ResultSet đang mở ở trên
        String updateFinishedSql = "UPDATE products SET status = 'FINISHED' " +
                "WHERE end_time <= NOW() AND status = 'RUNNING'";

        try (Connection conn = getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateFinishedSql)) {
            int rows = updateStmt.executeUpdate();
            System.out.println("[HỆ THỐNG] Đã đổi " + rows + " sản phẩm sang FINISHED");
        } catch (SQLException e) {
            System.err.println("Lỗi đổi trạng thái FINISHED: " + e.getMessage());
            e.printStackTrace();
        }
    }
    //BỔ SUNG HÀM THANH TOÁN GỘP TRANSACTION
    public static boolean executeManualPayment(String username, String productId, double amount) {
        // Thêm tham số sellerName, hoặc query seller bên trong
        String getSellerSql = "SELECT seller_name FROM products WHERE id = ?";
        String updateBuyerSql  = "UPDATE accounts SET balance = balance - ? WHERE username = ? AND balance >= ?";
        String updateSellerSql = "UPDATE accounts SET balance = balance + ? WHERE username = ?";
        String updateHistorySql = "UPDATE bid_history SET is_paid = 1 WHERE bidder_name = ? AND product_id = ? AND result = 'WIN'";
        String updateProductSql = "UPDATE products SET status = 'PAID' WHERE id = ? AND status = 'FINISHED'"; // Thêm điều kiện AND status = 'FINISHED' để idempotent

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 0. Lấy tên seller
                String sellerName = null;
                try (PreparedStatement ps0 = conn.prepareStatement(getSellerSql)) {
                    ps0.setString(1, productId);
                    ResultSet rs = ps0.executeQuery();
                    if (rs.next()) sellerName = rs.getString("seller_name");
                }

                // 1. Trừ tiền buyer (có kiểm tra đủ tiền tại DB — AND balance >= ?)
                int rows;
                try (PreparedStatement ps1 = conn.prepareStatement(updateBuyerSql)) {
                    ps1.setDouble(1, amount);
                    ps1.setString(2, username);
                    ps1.setDouble(3, amount); // điều kiện: balance >= amount
                    rows = ps1.executeUpdate();
                }
                if (rows == 0) throw new SQLException("Số dư không đủ hoặc đã thanh toán.");

                // 2. Cộng tiền seller
                if (sellerName != null && !sellerName.trim().isEmpty()) {
                    try (PreparedStatement ps2 = conn.prepareStatement(updateSellerSql)) {
                        ps2.setDouble(1, amount);
                        ps2.setString(2, sellerName);
                        ps2.executeUpdate();
                    }
                }

                // 3. Đánh dấu is_paid
                try (PreparedStatement ps3 = conn.prepareStatement(updateHistorySql)) {
                    ps3.setString(1, username);
                    ps3.setString(2, productId);
                    ps3.executeUpdate();
                }

                // 4. Đổi status sản phẩm
                try (PreparedStatement ps4 = conn.prepareStatement(updateProductSql)) {
                    ps4.setString(1, productId);
                    ps4.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 1. Hàm lưu khi người dùng bấm "Bật Auto Bid"
    public static boolean setupAutoBid(String username, String productId, double maxPrice) {
        String sql = "INSERT INTO auto_bids (username, product_id, max_price) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE max_price = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, productId);
            stmt.setDouble(3, maxPrice);
            stmt.setDouble(4, maxPrice);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. Hàm kích hoạt tự động nâng giá khi có người khác đặt giá cao hơn
    public static void triggerAutoBidSystem(String productId, double currentPrice, double bidIncrement) {
        try (Connection conn = getConnection()) {

            while (true) {
                // Tìm người AUTO BID cao nhất CÒN ĐỦ ĐIỀU KIỆN
                String sql = """
                SELECT ab.username, ab.max_price 
                FROM auto_bids ab
                WHERE ab.product_id = ? 
                  AND ab.max_price >= ?
                  AND ab.username != (SELECT highest_bidder FROM products WHERE id = ?)
                ORDER BY ab.max_price DESC 
                LIMIT 1
            """;

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, productId);
                    stmt.setDouble(2, currentPrice + bidIncrement);
                    stmt.setString(3, productId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            // Không còn ai có thể vượt giá → DỪNG
                            break;
                        }

                        String autoUser = rs.getString("username");
                        double maxPrice = rs.getDouble("max_price");
                        double newPrice = currentPrice + bidIncrement;

                        // Kiểm tra số dư
                        double balance = getBalance(autoUser);
                        if (balance < newPrice) {
                            // Xóa AutoBid, thử người tiếp theo
                            String del = "DELETE FROM auto_bids WHERE username = ? AND product_id = ?";
                            try (PreparedStatement delStmt = conn.prepareStatement(del)) {
                                delStmt.setString(1, autoUser);
                                delStmt.setString(2, productId);
                                delStmt.executeUpdate();
                            }
                            continue; // Thử lại vòng lặp với người khác
                        }

                        // Lấy người giữ giá cũ để hoàn tiền
                        Product p = findProductById(productId);
                        String oldBidder = p != null ? p.getHighestBidder() : null;
                        double oldPrice  = p != null ? p.getCurrentPrice()  : 0;

                        // Cập nhật giá
                        String upd = "UPDATE products SET current_price = ?, highest_bidder = ? WHERE id = ? AND status = 'RUNNING'";
                        try (PreparedStatement updStmt = conn.prepareStatement(upd)) {
                            updStmt.setDouble(1, newPrice);
                            updStmt.setString(2, autoUser);
                            updStmt.setString(3, productId);
                            if (updStmt.executeUpdate() == 0) break; // Phiên đã đóng → dừng
                        }

                        // Trừ tiền người vừa thắng
                        updateBalance(autoUser, -newPrice);

                        // Hoàn tiền người bị vượt
                        if (oldBidder != null && !oldBidder.equals("None") && !oldBidder.equals(autoUser)) {
                            updateBalance(oldBidder, oldPrice);
                        }

                        // Ghi log biểu đồ
                        String log = "INSERT INTO product_price_log (product_id, bidder_name, price_milestone) VALUES (?, ?, ?)";
                        try (PreparedStatement logStmt = conn.prepareStatement(log)) {
                            logStmt.setString(1, productId);
                            logStmt.setString(2, autoUser);
                            logStmt.setDouble(3, newPrice);
                            logStmt.executeUpdate();
                        }

                        System.out.println("[AutoBid] " + autoUser + " → " + newPrice);

                        // Cập nhật giá để vòng lặp tiếp tục đúng
                        currentPrice = newPrice;

                        // Nếu đã đạt max_price của người này → xóa AutoBid của họ
                        if (newPrice >= maxPrice) {
                            String del = "DELETE FROM auto_bids WHERE username = ? AND product_id = ?";
                            try (PreparedStatement delStmt = conn.prepareStatement(del)) {
                                delStmt.setString(1, autoUser);
                                delStmt.setString(2, productId);
                                delStmt.executeUpdate();
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Trả về raw data cho biểu đồ - Serializable để gửi qua network
    public static List<String[]> getRawChartData(String productId) {
        List<String[]> points = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(recorded_at, '%H:%i:%s') as bid_time, price_milestone " +
                "FROM product_price_log WHERE product_id = ? ORDER BY recorded_at ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    points.add(new String[]{
                            rs.getString("bid_time"),
                            String.valueOf(rs.getDouble("price_milestone"))
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return points;
    }

    public static XYChart.Series<String, Number> getProductChartData(String productId, String productName) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(productName); // Tên sản phẩm hiển thị ở chú thích biểu đồ

        // Lấy Giờ:Phút:Giây và Mức giá tăng dần theo thời gian
        String sql = "SELECT DATE_FORMAT(recorded_at, '%H:%i:%s') as bid_time, price_milestone " +
                "FROM product_price_log WHERE product_id = ? ORDER BY recorded_at ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String time = rs.getString("bid_time");         // Trục X (Thời gian)
                    double price = rs.getDouble("price_milestone"); // Trục Y (Giá tiền)

                    series.getData().add(new XYChart.Data<>(time, price));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return series;
    }

    private static void extendIfLastMinutes(Connection conn, String productId) {
        String checkSql = "SELECT end_time FROM products WHERE id = ? AND status = 'RUNNING'";
        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setString(1, productId);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    Timestamp endTs = rs.getTimestamp("end_time");
                    if (endTs == null) return;

                    LocalDateTime endTime = endTs.toLocalDateTime();
                    LocalDateTime now = LocalDateTime.now();
                    long secondsLeft = java.time.temporal.ChronoUnit.SECONDS.between(now, endTime);

                    // Chỉ kéo dài nếu còn dưới 5 phút (300 giây)
                    if (secondsLeft > 0 && secondsLeft < 300) {
                        String extendSql = "UPDATE products SET end_time = DATE_ADD(end_time, INTERVAL 5 MINUTE) WHERE id = ?";
                        try (PreparedStatement extend = conn.prepareStatement(extendSql)) {
                            extend.setString(1, productId);
                            extend.executeUpdate();
                            System.out.println("[Anti-Snipe] Kéo dài thêm 5 phút cho SP: " + productId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}