package com.example.onlineauctionsystem.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStorageBackup {
    public static List<Account> accounts = new ArrayList<>();
    public static List<Product> products = new ArrayList<>();

    // --- LOGIC TÀI KHOẢN ---
    public static Account checkLogin(String username, String password) {
        for (Account acc : accounts) {
            if (acc.getUsername().equals(username) && acc.getPassword().equals(password)) {
                return acc;
            }
        }
        return null;
    }

    public static boolean register(Account newAcc) {
        for (Account acc : accounts) {
            if (acc.getUsername().equals(newAcc.getUsername())) return false; // Trùng user
        }
        accounts.add(newAcc);
        saveToFiles(); // Lưu lại ngay khi có người đăng ký
        return true;
    }

    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        for (Account acc : accounts) {
            if (acc.getUsername().equals(username) && acc.getPassword().equals(oldPassword)) {
                acc.setPassword(newPassword);
                saveToFiles(); // Lưu lại ngay khi đổi pass
                return true;
            }
        }
        return false;
    }

    // --- LOGIC SẢN PHẨM ---
    public static Product findProductById(String id) {
        for (Product p : products) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    public static void addProduct(Product p) {
        products.add(p);
        saveToFiles();
    }

    public static boolean deleteProduct(String id) {
        Product p = findProductById(id);
        // Chỉ cho xóa nếu chưa kết thúc hoặc chưa có ai đấu giá
        if (p != null && !p.getStatus().equals("RUNNING")) {
            products.remove(p);
            saveToFiles();
            return true;
        }
        return false;
    }

    // --- LOGIC LƯU TRỮ (PERSISTENCE) ---
    public static void saveToFiles() {
        try (ObjectOutputStream oosAccounts = new ObjectOutputStream(new FileOutputStream("accounts.dat"));
             ObjectOutputStream oosProducts = new ObjectOutputStream(new FileOutputStream("products.dat"))) {
            oosAccounts.writeObject(accounts);
            oosProducts.writeObject(products);
        } catch (IOException e) {
            System.out.println("Lỗi lưu file: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadFromFiles() {
        try (ObjectInputStream oisAccounts = new ObjectInputStream(new FileInputStream("accounts.dat"));
             ObjectInputStream oisProducts = new ObjectInputStream(new FileInputStream("products.dat"))) {
            accounts = (List<Account>) oisAccounts.readObject();
            products = (List<Product>) oisProducts.readObject();
            System.out.println("Đã tải dữ liệu từ file thành công!");
        } catch (Exception e) {
            System.out.println("Chưa có file dữ liệu, tiến hành tạo dữ liệu mẫu...");
            initData(); // Gọi hàm tạo dữ liệu mẫu nếu file chưa tồn tại
        }
    }

    private static void initData() {
        accounts.add(new Account("admin", "admin123", "ADMIN"));
        accounts.add(new Account("hieu", "123", "BIDDER"));
        // Bạn có thể thêm các product mẫu vào đây
        saveToFiles();
    }
}