package com.example.onlineauctionsystem.model;

import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.Validator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    private static final String URL = "jdbc:mysql://localhost:3306/online_auction";
    private static final String USER = "root";
    private static final String PASS = "";
    public static Account currentAccount;
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
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
            String sql = "UPDATE accounts SET id_card = ?, email = ?, phone_number = ? WHERE username = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, acc.getIdCard());     // Khớp với id_card
                stmt.setString(2, acc.getEmail());      // Khớp với email
                stmt.setString(3, acc.getPhoneNumber());// Khớp với phone_number
                stmt.setString(4, acc.getUsername());   // Điều kiện để biết sửa ai

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
                Timestamp startTs = rs.getTimestamp("start_time");
                if (startTs != null) {
                    p.setStartTime(startTs.toLocalDateTime()); // Đã duyệt rồi thì lấy start_time lên đây!
                }
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
        // Nếu 2 người cùng gửi, người nào vào DB trước thắng
        // Người sau sẽ bị từ chối vì newPrice <= current_price mới
        String sql = "UPDATE products SET current_price = ?, highest_bidder = ? " +
                "WHERE id = ? AND status = 'RUNNING' AND current_price < ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newPrice);
            stmt.setString(2, bidderName);
            stmt.setString(3, productId);
            stmt.setDouble(4, newPrice); // ← điều kiện: giá hiện tại < giá mới
            int rows = stmt.executeUpdate();
            return rows > 0; // 0 = bị người khác đặt trước
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
                Timestamp startTs = rs.getTimestamp("start_time");
                if (startTs != null) {
                    p.setStartTime(startTs.toLocalDateTime());
                }
                p.updateStatus(); // Tự cập nhật trạng thái theo thời gian thực
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
    public static boolean saveBidHistory(String bidderName, String productId,
                                         String productName, double myBid, double finalPrice,
                                         LocalDateTime endTime, String result, boolean isPaid) {
        String sql = "INSERT INTO bid_history (bidder_name, product_id, product_name, " +
                "my_bid_price, final_price, end_time, result, is_paid) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bidderName);
            stmt.setString(2, productId);
            stmt.setString(3, productName);
            stmt.setDouble(4, myBid);
            stmt.setDouble(5, finalPrice);
            stmt.setTimestamp(6, Timestamp.valueOf(endTime));
            stmt.setString(7, result);
            stmt.setBoolean(8,isPaid);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
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
        // 1. Lấy danh sách sản phẩm đã hết giờ (end_time <= NOW) nhưng chưa có trong bảng bid_history
        String findExpiredSql = "SELECT id, name, current_price, highest_bidder FROM products " +
                "WHERE end_time <= NOW() " +
                "AND id NOT IN (SELECT DISTINCT product_id FROM bid_history)";

        // 2. Câu lệnh lưu vào bảng lịch sử đấu giá
        String insertHistorySql = "INSERT INTO bid_history (bidder_name, product_id, product_name, my_bid_price, final_price, end_time, result, is_paid) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), ?, 0)";

        // 3. Câu lệnh tìm tất cả những người đã từng ra giá cho sản phẩm này (để lưu trạng thái THUA)
        String findAllBiddersSql = "SELECT DISTINCT bidder_name, MAX(bid_amount) as max_bid FROM bids " +
                "WHERE product_id = ? AND bidder_name != ? GROUP BY bidder_name";

        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(findExpiredSql);
             ResultSet rs = selectStmt.executeQuery()) {

            while (rs.next()) {
                int productId = rs.getInt("id");
                String productName = rs.getString("name");
                double finalPrice = rs.getDouble("current_price");
                String winner = rs.getString("highest_bidder");

                // TRƯỜNG HỢP 1: CÓ NGƯỜI THẮNG CUỘC (highest_bidder không thô/null)
                if (winner != null && !winner.trim().isEmpty()) {

                    // A. Lưu người THẮNG (WIN) vào bảng lịch sử
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertHistorySql)) {
                        insertStmt.setString(1, winner);
                        insertStmt.setInt(2, productId);
                        insertStmt.setString(3, productName);
                        insertStmt.setDouble(4, finalPrice); // Giá họ trả trúng bằng giá cuối
                        insertStmt.setDouble(5, finalPrice);
                        insertStmt.setString(6, "WIN");
                        insertStmt.executeUpdate();
                    }

                    // B. Tìm tất cả những người khác từng đấu giá sản phẩm này nhưng thất bại để lưu THUA (LOSE)
                    try (PreparedStatement bidderStmt = conn.prepareStatement(findAllBiddersSql)) {
                        bidderStmt.setInt(1, productId);
                        bidderStmt.setString(2, winner); // Loại trừ người thắng ra
                        try (ResultSet rsBidders = bidderStmt.executeQuery()) {
                            while (rsBidders.next()) {
                                String loserName = rsBidders.getString("bidder_name");
                                double loserMaxBid = rsBidders.getDouble("max_bid");

                                // Lưu người THUA vào lịch sử
                                try (PreparedStatement insertLooseStmt = conn.prepareStatement(insertHistorySql)) {
                                    insertLooseStmt.setString(1, loserName);
                                    insertLooseStmt.setInt(2, productId);
                                    insertLooseStmt.setString(3, productName);
                                    insertLooseStmt.setDouble(4, loserMaxBid); // Mức giá lớn nhất họ từng trả
                                    insertLooseStmt.setDouble(5, finalPrice);  // Giá cuối cùng của sản phẩm
                                    insertLooseStmt.setString(6, "LOSE");
                                    insertLooseStmt.executeUpdate();
                                }
                            }
                        }
                    }
                    System.out.println("[HỆ THỐNG] Đã chốt phiên tự động và lưu lịch sử cho sản phẩm ID: " + productId + " (Người thắng: " + winner + ")");
                } else {
                    // TRƯỜNG HỢP 2: Phiên đấu giá kết thúc nhưng KHÔNG AI THAM GIA ĐẤU
                    // Có thể bỏ qua hoặc lưu trạng thái sản phẩm bị hủy tùy nhu cầu của bạn.
                    System.out.println("[HỆ THỐNG] Phiên đấu giá ID: " + productId + " kết thúc nhưng không có người tham gia.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tự động chốt phiên và lưu lịch sử: " + e.getMessage());
            e.printStackTrace();
        }
    }
}