package com.example.onlineauctionsystem.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    public static List<Account> accounts = new ArrayList<>();
    public static List<Product> products = new ArrayList<>();

    public static void initData() {
        accounts.clear();
        products.clear();
        // Tài khoản mẫu
        accounts.add(new Bidder("hieu", "123", 5000.0));
        accounts.add(new Seller("admin", "admin"));

        // Sản phẩm mẫu: Bắt đầu từ bây giờ, kết thúc sau 2 tiếng
        products.add(new Product("P01", "Laptop Gaming", "Core i9, RTX 4090", 1500.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), "admin"));
    }

    // LOGIC 3.1.1: Đăng ký tài khoản
    public static boolean register(Account newAccount) {
        for (Account a : accounts) {
            if (a.getUsername().equals(newAccount.getUsername())) return false;
        }
        accounts.add(newAccount);
        return true;
    }

    public static Account checkLogin(String user, String pass) {
        for (Account a : accounts) {
            if (a.getUsername().equals(user) && a.getPassword().equals(pass)) return a;
        }
        return null;
    }
}