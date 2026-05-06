package com.example.onlineauctionsystem.model;

import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.Validator;
import com.mysql.cj.x.protobuf.MysqlxCrud;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    private static final String URL = "jdbc:mysql://localhost:3306/online_auction";
    private static final String USER = "root";
    private static final String PASS = "";

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
                return new Account(rs.getString("username"), rs.getString("password"), rs.getString("role"));
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

        String sql = "INSERT INTO accounts (username, password, role, id_card, email, phone_number) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, acc.getUsername());
            stmt.setString(2, acc.getPassword());
            stmt.setString(3, acc.getRole());
            stmt.setString(4, acc.getIdCard());
            stmt.setString(5, acc.getEmail());
            stmt.setString(6, acc.getPhoneNumber());

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
                        rs.getString("id"), rs.getString("name"), rs.getString("description"),
                        rs.getDouble("initial_price"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getString("seller_name")
                );
                p.setCurrentPrice(rs.getDouble("current_price"));
                p.setHighestBidder(rs.getString("highest_bidder"));
                p.setStatus(rs.getString("status"));
                p.updateStatus();
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean addProduct(Product p) {
        String sql = "INSERT INTO products (id, name, description, initial_price, current_price, start_time, end_time, status, seller_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getId());
            stmt.setString(2, p.getName());
            stmt.setString(3, p.getDescription());
            stmt.setDouble(4, p.getInitialPrice());
            stmt.setDouble(5, p.getCurrentPrice());
            stmt.setTimestamp(6, Timestamp.valueOf(p.getStartTime()));
            stmt.setTimestamp(7, Timestamp.valueOf(p.getEndTime()));
            stmt.setString(8, p.getStatus());
            stmt.setString(9, p.getSellerName());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean updateBid(String productId, double newPrice, String bidderName) {
        String sql = "UPDATE products SET current_price = ?, highest_bidder = ? WHERE id = ? AND status = 'RUNNING'";
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
                        rs.getString("id"), rs.getString("name"), rs.getString("description"),
                        rs.getDouble("initial_price"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getString("seller_name")
                );
                p.setCurrentPrice(rs.getDouble("current_price"));
                p.setHighestBidder(rs.getString("highest_bidder"));
                p.setStatus(rs.getString("status"));
                p.updateStatus(); // Tự cập nhật trạng thái theo thời gian thực
                return p;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
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