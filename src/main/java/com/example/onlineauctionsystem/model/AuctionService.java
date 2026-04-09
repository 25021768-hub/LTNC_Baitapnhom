package com.example.onlineauctionsystem.model;

import java.util.List;

/**
 * AuctionService: Bộ não điều phối toàn bộ logic của hệ thống đấu giá.
 */
public class AuctionService {

    /**
     * Hàm trung tâm xử lý mọi yêu cầu từ mạng gửi đến.
     */
    public static AuctionMessage handleRequest(AuctionMessage request) {
        try {
            return switch (request.getAction()) {
                case LOGIN -> handleLogin(request);
                case REGISTER -> handleRegister(request);
                case BID -> handleBid(request);
                case UPDATE_LIST -> handleUpdateList();
                case ADD_PRODUCT -> handleAddProduct(request);
                case DELETE_PRODUCT -> handleDeleteProduct(request);
                default -> new AuctionMessage(AuctionMessage.Action.ERROR, "Hành động không xác định!");
            };
        } catch (Exception e) {
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Lỗi xử lý: " + e.getMessage());
        }
    }

    // 1. Xử lý Đăng nhập
    private static AuctionMessage handleLogin(AuctionMessage request) {
        String[] info = (String[]) request.getData();
        Account acc = DataStorage.checkLogin(info[0], info[1]);
        if (acc != null) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, acc);
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Sai tài khoản hoặc mật khẩu!");
    }

    // 2. Xử lý Đăng ký
    private static AuctionMessage handleRegister(AuctionMessage request) {
        Account newAcc = (Account) request.getData();
        if (DataStorage.register(newAcc)) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, "Đăng ký thành công!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Tài khoản đã tồn tại!");
    }

    // 3. Xử lý Trả giá (Real-time)
    private static AuctionMessage handleBid(AuctionMessage request) {
        Object[] bidData = (Object[]) request.getData();
        String pId = (String) bidData[0];
        double amount = (double) bidData[1];
        String name = (String) bidData[2];

        Product p = DataStorage.findProductById(pId);
        if (p != null) {
            if (p.placeBid(amount, name)) {
                return new AuctionMessage(AuctionMessage.Action.SUCCESS, p);
            }
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Giá thầu không hợp lệ hoặc phiên đã đóng!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Sản phẩm không tồn tại!");
    }

    // 4. Cập nhật danh sách sản phẩm mới nhất
    private static AuctionMessage handleUpdateList() {
        for (Product p : DataStorage.products) {
            p.updateStatus(); // Đồng bộ thời gian thực cho từng món hàng
        }
        return new AuctionMessage(AuctionMessage.Action.SUCCESS, DataStorage.products);
    }

    // 5. Thêm sản phẩm mới (Dành cho Seller/Admin)
    private static AuctionMessage handleAddProduct(AuctionMessage request) {
        Product newP = (Product) request.getData();
        DataStorage.addProduct(newP);
        return new AuctionMessage(AuctionMessage.Action.SUCCESS, "Đã thêm sản phẩm thành công!");
    }

    // 6. Xóa sản phẩm (Dành cho Admin/Seller)
    private static AuctionMessage handleDeleteProduct(AuctionMessage request) {
        String idToDelete = (String) request.getData();
        if (DataStorage.deleteProduct(idToDelete)) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, "Đã xóa sản phẩm!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Không thể xóa (Sản phẩm đang đấu giá hoặc không tồn tại)!");
    }
}