package com.example.onlineauctionsystem.model;

public class AuctionService {

    public static AuctionMessage handleRequest(AuctionMessage request) {
        try {
            // Sử dụng Enhanced Switch (không còn gạch vàng)
            return switch (request.getAction()) {
                case LOGIN -> handleLogin(request);
                case REGISTER -> handleRegister(request);
                case BID -> handleBid(request);
                case UPDATE_LIST -> handleUpdateList();
                case ADD_PRODUCT -> handleAddProduct(request);
                case DELETE_PRODUCT -> handleDeleteProduct(request);
                case CHANGE_PASSWORD -> handleChangePassword(request);
                default -> new AuctionMessage(AuctionMessage.Action.ERROR, "Hành động không hợp lệ!");
            };
        } catch (Exception e) {
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    private static AuctionMessage handleLogin(AuctionMessage request) {
        String[] info = (String[]) request.getData();
        Account acc = DataStorageBackup.checkLogin(info[0], info[1]);
        if (acc != null) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, acc);
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Sai tài khoản hoặc mật khẩu!");
    }

    private static AuctionMessage handleRegister(AuctionMessage request) {
        Account newAcc = (Account) request.getData();
        if (DataStorageBackup.register(newAcc)) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, "Đăng ký thành công!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Tài khoản đã tồn tại!");
    }

    private static AuctionMessage handleChangePassword(AuctionMessage request) {
        String[] data = (String[]) request.getData();
        String username = data[0];
        String oldPass = data[1];
        String newPass = data[2];

        if (DataStorageBackup.changePassword(username, oldPass, newPass)) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, "Đổi mật khẩu thành công!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Sai mật khẩu cũ!");
    }

    private static AuctionMessage handleBid(AuctionMessage request) {
        Object[] bidData = (Object[]) request.getData();
        String pId = (String) bidData[0];
        double amount = (double) bidData[1];
        String name = (String) bidData[2];

        Product p = DataStorageBackup.findProductById(pId);
        if (p != null) {
            if (p.placeBid(amount, name)) {
                DataStorageBackup.saveToFiles(); // Lưu ngay khi có giá mới
                return new AuctionMessage(AuctionMessage.Action.SUCCESS, p);
            }
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Giá thấp hơn hoặc phiên đấu giá đã đóng!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Sản phẩm không tồn tại!");
    }

    private static AuctionMessage handleUpdateList() {
        for (Product p : DataStorageBackup.products) {
            p.updateStatus();
        }
        return new AuctionMessage(AuctionMessage.Action.SUCCESS, DataStorageBackup.products);
    }

    private static AuctionMessage handleAddProduct(AuctionMessage request) {
        Product newP = (Product) request.getData();
        DataStorageBackup.addProduct(newP);
        return new AuctionMessage(AuctionMessage.Action.SUCCESS, "Thêm sản phẩm thành công!");
    }

    private static AuctionMessage handleDeleteProduct(AuctionMessage request) {
        String id = (String) request.getData();
        if (DataStorageBackup.deleteProduct(id)) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, "Xóa sản phẩm thành công!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Không thể xóa sản phẩm đang đấu giá!");
    }
}