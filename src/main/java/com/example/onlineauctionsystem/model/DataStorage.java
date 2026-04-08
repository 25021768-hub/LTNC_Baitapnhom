package com.example.onlineauctionsystem.model;

import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    // Dùng List<Account> để chứa được cả Bidder và Seller (Tính Đa hình)
    public static List<Account> accounts = new ArrayList<>();
    public static List<Product> products = new ArrayList<>();

    public static void initData() {
        accounts.clear();
        products.clear();

        // Thêm dữ liệu mẫu cực kỳ đơn giản
        accounts.add(new Bidder("hieu", "123", 1000.0));
        accounts.add(new Seller("admin", "admin"));

        products.add(new Product("Laptop Dell", 1200.0, "admin"));
    }
}