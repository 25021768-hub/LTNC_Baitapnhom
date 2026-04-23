package com.example.onlineauctionsystem.model; // Thay tên này nếu package của bạn khác

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        // Thông tin kết nối đến XAMPP MySQL
        String url = "jdbc:mysql://localhost:3306/online_auction";
        String user = "root";
        String pass = "";

        try {
            System.out.println("Đang kết nối thử...");
            Connection conn = DriverManager.getConnection(url, user, pass);

            System.out.println("🎉 KẾT NỐI DATABASE THÀNH CÔNG!");

            // Thử lấy dữ liệu từ bảng accounts đã tạo ở Bước 2
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM accounts");

            while (rs.next()) {
                System.out.println("-> Tìm thấy tài khoản trong DB: " + rs.getString("username"));
            }

            conn.close();
        } catch (Exception e) {
            System.out.println("❌ LỖI KẾT NỐI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}