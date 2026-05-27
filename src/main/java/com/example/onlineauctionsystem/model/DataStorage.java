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
        String sql = "UPDATE products SET current_price = ?, highest_bidder = ?, " +
                "bid_increment = bid_increment + 1 " +
                "WHERE id = ? AND status = 'RUNNING'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newPrice);
            stmt.setString(2, bidderName);
            stmt.setString(3, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
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

    public static void closeExpiredAuctions() {
        // SQL: Chuyển từ RUNNING sang FINISHED nếu thời gian hiện tại đã vượt quá end_time
        String sql = "UPDATE products SET status = 'FINISHED' " +
                "WHERE status = 'RUNNING' AND end_time <= NOW()";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("[Server] Đã tự động đóng " + affectedRows + " phiên đấu giá hết hạn.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}