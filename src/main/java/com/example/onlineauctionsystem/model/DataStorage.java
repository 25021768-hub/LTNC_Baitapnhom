package com.example.onlineauctionsystem.model;

import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    public static List<Account> accounts = new ArrayList<>();
    public static List<Product> products = new ArrayList<>();

    // Khởi tạo dữ liệu mẫu (Mock data) để các bạn khác có cái dùng luôn
    public static void initData() {
        accounts.clear();
        products.clear();

        // Thêm tài khoản mẫu
        accounts.add(new Bidder("hieu", "123", 5000.0));
        accounts.add(new Seller("admin", "admin"));

        // Thêm sản phẩm mẫu
        products.add(new Product("P01", "Laptop Gaming", 1500.0, "admin"));
        products.add(new Product("P02", "iPhone 15 Pro", 1000.0, "admin"));
    }

    // LOGIC: Kiểm tra đăng nhập
    public static Account checkLogin(String user, String pass) {
        for (Account a : accounts) {
            if (a.getUsername().equals(user) && a.getPassword().equals(pass)) {
                return a;
            }
        }
        return null;
    }

    // LOGIC: Tìm sản phẩm theo ID để trả giá
    public static Product findProductById(String id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}