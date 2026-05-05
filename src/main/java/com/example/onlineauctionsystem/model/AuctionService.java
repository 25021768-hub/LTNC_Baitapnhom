package com.example.onlineauctionsystem.model;

import com.example.onlineauctionsystem.utils.Validator;
import java.util.List;

public class AuctionService {

    // 1. BỘ ĐỊNH TUYẾN
    public static AuctionMessage handleRequest(AuctionMessage request) {
        try {
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
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Lỗi Server: " + e.getMessage());
        }
    }

    private static AuctionMessage handleLogin(AuctionMessage request) {
        String[] info = (String[]) request.getData();
        Account acc = DataStorage.checkLogin(info[0], info[1]);
        if (acc != null) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, acc);
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Sai tài khoản hoặc mật khẩu!");
    }

    private static AuctionMessage handleRegister(AuctionMessage request) {
        Account newAcc = (Account) request.getData();

        // Gọi Validator kiểm tra Username
        if (!Validator.isValidUsername(newAcc.getUsername())) {
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Định dạng Username không hợp lệ!");
        }

        //Gọi validator kiểm tra email
        if (!Validator.isValidEmail(newAcc.getEmail())) {
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Định dạng Email không ợp lệ");
        }

        //Gọi Valid kiểm tra CCCD
        if (!Validator.isValidCCCD(newAcc.getIdCard())) {
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Vui lòng nhập đúng 12 số căn cước công dân");
        }

        //Gọi valid kiểm tra Số điện thoại
        if (!Validator.isValidPhone(newAcc.getPhoneNumber())) {
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Vui lòng nhập đúng số điện thoại");
        }

        // Kiểm tra tồn tại
        if (DataStorage.isAccountExists(newAcc.getUsername())) {
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Tài khoản (Username/Email/CCCD) đã tồn tại!");
        }

        // Lưu vào MySQL
        if (DataStorage.register(newAcc)) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, "Đăng ký thành công!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Lỗi khi lưu vào Database!");
    }

    private static AuctionMessage handleChangePassword(AuctionMessage request) {
        String[] data = (String[]) request.getData();
        if (DataStorage.changePassword(data[0], data[1], data[2])) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, "Đổi mật khẩu thành công!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Sai mật khẩu cũ!");
    }

    // 2. HÀM ĐẶT GIÁ
    private static synchronized AuctionMessage handleBid(AuctionMessage request) {
        Object[] bidData = (Object[]) request.getData();
        String pId = (String) bidData[0];
        double amount = (double) bidData[1];
        String name = (String) bidData[2];

        // 1: Kiểm tra Account tồn tại không
        if (!DataStorage.isAccountExists(name)) {
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Tài khoản không tồn tại!");
        }

        // 2: Lấy số dư từ DB và gọi Validator kiểm tra tiền
        double currentBalance = DataStorage.getBalance(name);
        if (!Validator.hasEnoughMoney(currentBalance, amount)) {
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Số dư của bạn (" + currentBalance + ") không đủ!");
        }

        // 3: Tìm sản phẩm và check logic giá
        Product p = DataStorage.findProductById(pId);
        if (p != null) {

            //Lưu lại để hoàn tiền
            String oldBidder = p.getHighestBidder();
            double oldPrice = p.getCurrentPrice();

            if (p.placeBid(amount, name)) { // Giả định placeBid trong Product return true nếu giá hợp lệ

                // 4: Lưu trực tiếp vào Database
                if (DataStorage.updateBid(pId, amount, name)) {

                    //Trừ tiền người vừa đặt giá thành công
                    DataStorage.updateBalance(name, -amount);

                    //Hoàn tiền cho người đã bị vượt giá
                    if (oldBidder != null && !oldBidder.trim().isEmpty()) {
                        DataStorage.updateBalance(oldBidder, oldPrice);
                    }

                    //Cập nhật lại object để trả về cho Client
                    p.setCurrentPrice(amount);
                    p.setHighestBidder(name);
                    return new AuctionMessage(AuctionMessage.Action.SUCCESS, p);
                } else {
                    return new AuctionMessage(AuctionMessage.Action.ERROR, "Lỗi cập nhật CSDL (Phiên có thể đã kết thúc)!");
                }
            }
            return new AuctionMessage(AuctionMessage.Action.ERROR, "Giá đưa ra quá thấp hoặc phiên đã đóng!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Sản phẩm không tồn tại!");
    }

    private static AuctionMessage handleUpdateList() {
        List<Product> list = DataStorage.getAllProducts();
        return new AuctionMessage(AuctionMessage.Action.SUCCESS, list);
    }

    private static AuctionMessage handleAddProduct(AuctionMessage request) {
        Product newP = (Product) request.getData();
        if (DataStorage.addProduct(newP)) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, "Thêm sản phẩm thành công!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Lỗi khi lưu sản phẩm!");
    }

    private static AuctionMessage handleDeleteProduct(AuctionMessage request) {
        // Do hàm deleteProduct (String id, Account loggedInUser), nên lúc gửi lên, Controller phải gói 2 object này vào mảng Object[]
        Object[] reqData = (Object[]) request.getData();
        String pId = (String) reqData[0];
        Account adminAcc = (Account) reqData[1];

        if (DataStorage.deleteProduct(pId, adminAcc)) {
            return new AuctionMessage(AuctionMessage.Action.SUCCESS, "Xóa sản phẩm thành công!");
        }
        return new AuctionMessage(AuctionMessage.Action.ERROR, "Xóa thất bại (Lỗi DB hoặc bạn không phải ADMIN)!");
    }
}