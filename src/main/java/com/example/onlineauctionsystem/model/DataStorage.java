package com.example.onlineauctionsystem.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    // Danh sách lưu trữ tập trung cho toàn bộ hệ thống
    public static List<Account> accounts = new ArrayList<>();
    public static List<Product> products = new ArrayList<>();

    /**
     * Khởi tạo dữ liệu mẫu (Mock data)
     * Đáp ứng mục 3.1.1 (Vai trò) và 3.1.2 (Sản phẩm mẫu)
     */
    public static void initData() {
        accounts.clear();
        products.clear();

        // 1. Thêm các tài khoản mẫu với đầy đủ 3 vai trò (Mục 3.1.1)
        accounts.add(new Bidder("hieu_bidder", "123", 5000.0));
        accounts.add(new Seller("admin_seller", "admin"));
        accounts.add(new Admin("root", "root123")); // Tài khoản Admin mới thêm

        // 2. Thêm các sản phẩm mẫu (Mục 3.1.2)
        // Sản phẩm 1: Đang diễn ra (Kết thúc sau 2 giờ)
        products.add(new Product(
                "P01",
                "Laptop Dell XPS 15",
                "Core i9, RAM 32GB, SSD 1TB - Like new 99%",
                1500.0,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                "admin_seller"
        ));

        // Sản phẩm 2: Sắp diễn ra (Bắt đầu sau 1 giờ, kết thúc sau 5 giờ)
        products.add(new Product(
                "P02",
                "iPhone 15 Pro Max",
                "Màu Titan tự nhiên, bản VN/A chính hãng",
                1200.0,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(5),
                "admin_seller"
        ));
    }

    // ==========================================
    // LOGIC QUẢN LÝ NGƯỜI DÙNG (Mục 3.1.1)
    // ==========================================

    public static Account checkLogin(String user, String pass) {
        for (Account a : accounts) {
            if (a.getUsername().equals(user) && a.getPassword().equals(pass)) {
                return a;
            }
        }
        return null;
    }

    public static boolean register(Account newAccount) {
        // Kiểm tra xem username đã tồn tại chưa
        for (Account a : accounts) {
            if (a.getUsername().equalsIgnoreCase(newAccount.getUsername())) {
                return false;
            }
        }
        accounts.add(newAccount);
        return true;
    }

    // ==========================================
    // LOGIC QUẢN LÝ SẢN PHẨM (Mục 3.1.2)
    // ==========================================

    public static void addProduct(Product p) {
        products.add(p);
    }

    public static boolean deleteProduct(String id) {
        // Chỉ cho phép xóa khi sản phẩm ở trạng thái OPEN (chưa bắt đầu)
        return products.removeIf(p -> p.getId().equals(id) && p.getStatus().equals("OPEN"));
    }

    public static boolean updateProduct(String id, String newName, String newDesc) {
        Product p = findProductById(id);
        if (p != null && p.getStatus().equals("OPEN")) {
            p.setName(newName);
            p.setDescription(newDesc);
            return true;
        }
        return false;
    }

    public static Product findProductById(String id) {
        for (Product p : products) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }
}