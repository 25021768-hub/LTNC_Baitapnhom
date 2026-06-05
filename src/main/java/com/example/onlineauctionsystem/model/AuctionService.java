package com.example.onlineauctionsystem.model;

import com.example.onlineauctionsystem.network.AuctionMessage;
import com.example.onlineauctionsystem.utils.Validator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  AuctionService  –  Tầng xử lý nghiệp vụ (Business Logic)
 * ╠══════════════════════════════════════════════════════════════╣
 *
 *  Đây là "bộ não" của Server:
 *  - Nhận AuctionMessage từ AuctionServer
 *  - Phân tích action, gọi DataStorage tương ứng
 *  - Trả về AuctionMessage kết quả
 *
 *  Không phụ thuộc vào JavaFX, có thể test độc lập.
 * ══════════════════════════════════════════════════════════════
 */
public class AuctionService {

    private AuctionService() {}

    // ──────────────────────────────────────────────────────────────
    //  BỘ ĐỊNH TUYẾN CHÍNH
    // ──────────────────────────────────────────────────────────────
    public static AuctionMessage handleRequest(AuctionMessage request) {
        if (request == null) {
            return error("Request rỗng.");
        }
        try {
            return switch (request.getAction()) {
                // Auth
                case LOGIN              -> handleLogin(request);
                case REGISTER           -> handleRegister(request);
                case CHANGE_PASSWORD    -> handleChangePassword(request);
                case FORGOT_PASSWORD    -> handleForgotPassword(request);
                case UPDATE_ACCOUNT     -> handleUpdateAccount(request);

                // Sản phẩm
                case GET_ALL_PRODUCTS   -> handleGetAllProducts();
                case GET_PRODUCT_BY_ID  -> handleGetProductById(request);
                case ADD_PRODUCT        -> handleAddProduct(request);
                case DELETE_PRODUCT     -> handleDeleteProduct(request);
                case DELETE_MY_PRODUCT  -> handleDeleteMyProduct(request);
                case UPDATE_PRODUCT_STATUS -> handleUpdateProductStatus(request);

                // Đấu giá
                case BID                -> handleBid(request);
                case SETUP_AUTO_BID     -> handleSetupAutoBid(request);
                case TRIGGER_AUTO_BID   -> handleTriggerAutoBid(request);

                // Lịch sử & Số dư
                case GET_BID_HISTORY    -> handleGetBidHistory(request);
                case GET_RUNNING_BIDS   -> handleGetRunningBids(request);
                case SAVE_BID_HISTORY   -> handleSaveBidHistory(request);
                case GET_BALANCE        -> handleGetBalance(request);
                case UPDATE_BALANCE     -> handleUpdateBalance(request);
                case EXECUTE_PAYMENT    -> handleExecutePayment(request);
                case GET_MAX_BID        -> handleGetMaxBid(request);
                case GET_CHART_DATA     -> handleGetChartData(request);

                // Admin
                case GET_ALL_ACCOUNTS   -> handleGetAllAccounts();
                case SET_ACCOUNT_LOCKED -> handleSetAccountLocked(request);
                case IS_ACCOUNT_EXISTS  -> handleIsAccountExists(request);

                default -> error("Hành động không được hỗ trợ: " + request.getAction());
            };
        } catch (ClassCastException e) {
            return error("Dữ liệu gửi lên không đúng kiểu: " + e.getMessage());
        } catch (Exception e) {
            return error("Lỗi Server nội bộ: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  AUTH
    // ──────────────────────────────────────────────────────────────

    private static AuctionMessage handleLogin(AuctionMessage req) {
        String[] info = (String[]) req.getData(); // [username, password]
        Account acc = DataStorage.checkLogin(info[0], info[1]);
        if (acc != null) return success(acc);
        return error("Sai tên đăng nhập hoặc mật khẩu!");
    }

    private static AuctionMessage handleRegister(AuctionMessage req) {
        Account newAcc = (Account) req.getData();

        if (!Validator.isValidUsername(newAcc.getUsername()))
            return error("Định dạng Username không hợp lệ!");
        if (!Validator.isValidEmail(newAcc.getEmail()))
            return error("Định dạng Email không hợp lệ!");
        if (!Validator.isValidCCCD(newAcc.getIdCard()))
            return error("Vui lòng nhập đúng 12 số căn cước công dân.");
        if (!Validator.isValidPhone(newAcc.getPhoneNumber()))
            return error("Vui lòng nhập đúng số điện thoại.");
        if (DataStorage.isAccountExists(newAcc.getUsername()))
            return error("Tài khoản (Username/Email/CCCD) đã tồn tại!");
        if (DataStorage.register(newAcc))
            return success("Đăng ký thành công!");
        return error("Lỗi khi lưu vào Database!");
    }

    private static AuctionMessage handleChangePassword(AuctionMessage req) {
        String[] data = (String[]) req.getData(); // [username, oldPass, newPass]
        if (DataStorage.changePassword(data[0], data[1], data[2]))
            return success("Đổi mật khẩu thành công!");
        return error("Sai mật khẩu cũ!");
    }

    private static AuctionMessage handleForgotPassword(AuctionMessage req) {
        String[] data = (String[]) req.getData(); // [identifier, newPass]
        if (DataStorage.changeForgotPassword(data[0], data[1]))
            return success("Đặt lại mật khẩu thành công!");
        return error("Không tìm thấy tài khoản với thông tin đã nhập.");
    }

    private static AuctionMessage handleUpdateAccount(AuctionMessage req) {
        Account acc = (Account) req.getData();
        if (DataStorage.updateAccount(acc))
            return success("Cập nhật tài khoản thành công!");
        return error("Lỗi cập nhật tài khoản.");
    }

    // ──────────────────────────────────────────────────────────────
    //  SẢN PHẨM
    // ──────────────────────────────────────────────────────────────

    private static AuctionMessage handleGetAllProducts() {
        List<Product> list = DataStorage.getAllProducts();
        return success(list);
    }

    private static AuctionMessage handleGetProductById(AuctionMessage req) {
        String pid = (String) req.getData();
        Product p = DataStorage.findProductById(pid);
        if (p != null) return success(p);
        return error("Không tìm thấy sản phẩm ID: " + pid);
    }

    private static AuctionMessage handleAddProduct(AuctionMessage req) {
        Product newP = (Product) req.getData();
        if (DataStorage.addProduct(newP))
            return success("Thêm sản phẩm thành công!");
        return error("Lỗi khi lưu sản phẩm!");
    }

    private static AuctionMessage handleDeleteProduct(AuctionMessage req) {
        // data: Object[] {String productId, Account adminAcc}
        Object[] data = (Object[]) req.getData();
        String pid      = (String)  data[0];
        Account admin   = (Account) data[1];
        if (DataStorage.deleteProduct(pid, admin))
            return success("Xóa sản phẩm thành công!");
        return error("Xóa thất bại (Lỗi DB hoặc bạn không phải ADMIN)!");
    }

    private static AuctionMessage handleDeleteMyProduct(AuctionMessage req) {
        // data: Object[] {String productId, String sellerUsername}
        Object[] data = (Object[]) req.getData();
        String pid            = (String) data[0];
        String sellerUsername = (String) data[1];
        if (DataStorage.deleteMyProduct(pid, sellerUsername))
            return success("Xóa sản phẩm thành công!");
        return error("Xóa thất bại hoặc bạn không có quyền xóa sản phẩm này.");
    }

    private static AuctionMessage handleUpdateProductStatus(AuctionMessage req) {
        // data: Object[] {String productId, String newStatus}
        Object[] data   = (Object[]) req.getData();
        String pid       = (String) data[0];
        String newStatus = (String) data[1];
        if (DataStorage.updateProductStatus(pid, newStatus))
            return success("Cập nhật trạng thái thành công!");
        return error("Lỗi cập nhật trạng thái sản phẩm.");
    }

    // ──────────────────────────────────────────────────────────────
    //  ĐẤU GIÁ
    // ──────────────────────────────────────────────────────────────

    /** synchronized: tránh race condition khi nhiều client đặt giá cùng lúc */
    private static synchronized AuctionMessage handleBid(AuctionMessage req) {
        // data: Object[] {String productId, double amount, String bidderUsername}
        Object[] bidData = (Object[]) req.getData();
        String pid    = (String) bidData[0];
        double amount = (double) bidData[1];
        String bidder = (String) bidData[2];

        // Kiểm tra tài khoản tồn tại
        if (!DataStorage.isAccountExists(bidder))
            return error("Tài khoản không tồn tại!");

        // Kiểm tra số dư
        double currentBalance = DataStorage.getBalance(bidder);
        if (!Validator.hasEnoughMoney(currentBalance, amount))
            return error(String.format("Số dư của bạn (%.0f) không đủ để đặt %.0f!", currentBalance, amount));

        // Kiểm tra sản phẩm
        Product p = DataStorage.findProductById(pid);
        if (p == null)
            return error("Sản phẩm không tồn tại!");

        if (p.getEndTime() != null && p.getEndTime().isBefore(LocalDateTime.now())) {
            DataStorage.autoCloseAndSaveExpiredProducts();
            return error("Phiên đấu giá này đã kết thúc!");
        }

        // Lưu thông tin người giữ giá cũ để hoàn tiền
        String oldBidder = p.getHighestBidder();
        double oldPrice  = p.getCurrentPrice();

        if (p.placeBid(amount, bidder)) {
            if (DataStorage.updateBid(pid, amount, bidder)) {
                // Trừ tiền người vừa thắng
                DataStorage.updateBalance(bidder, -amount);

                // Hoàn tiền người bị vượt giá
                if (oldBidder != null && !oldBidder.isBlank() && !"None".equals(oldBidder)) {
                    DataStorage.updateBalance(oldBidder, oldPrice);
                }

                // Kích hoạt auto-bid của người khác
                DataStorage.triggerAutoBidSystem(pid, amount, p.getBidIncrement());

                // Cập nhật object và trả về
                p.setCurrentPrice(amount);
                p.setHighestBidder(bidder);
                return success(p);
            }
            return error("Lỗi cập nhật CSDL (Phiên có thể đã kết thúc)!");
        }
        return error("Giá đưa ra quá thấp hoặc phiên đã đóng!");
    }

    private static AuctionMessage handleSetupAutoBid(AuctionMessage req) {
        // data: Object[] {String username, String productId, double maxPrice}
        Object[] data  = (Object[]) req.getData();
        String username = (String) data[0];
        String pid      = (String) data[1];
        double maxPrice = (double) data[2];
        if (DataStorage.setupAutoBid(username, pid, maxPrice))
            return success("Bật Auto Bid thành công!");
        return error("Lỗi cài đặt Auto Bid.");
    }

    private static AuctionMessage handleTriggerAutoBid(AuctionMessage req) {
        // data: Object[] {String productId, double currentPrice, double bidIncrement}
        Object[] data     = (Object[]) req.getData();
        String pid         = (String) data[0];
        double currentPrice= (double) data[1];
        double increment   = (double) data[2];
        DataStorage.triggerAutoBidSystem(pid, currentPrice, increment);
        return success("Auto Bid triggered.");
    }

    // ──────────────────────────────────────────────────────────────
    //  LỊCH SỬ & SỐ DƯ
    // ──────────────────────────────────────────────────────────────

    private static AuctionMessage handleGetBidHistory(AuctionMessage req) {
        String username = (String) req.getData();
        return success(DataStorage.getBidHistory(username));
    }

    private static AuctionMessage handleGetRunningBids(AuctionMessage req) {
        String username = (String) req.getData();
        return success(DataStorage.getRunningBidsByBidder(username));
    }

    private static AuctionMessage handleSaveBidHistory(AuctionMessage req) {
        BidHistory bh = (BidHistory) req.getData();
        if (DataStorage.saveBidHistory(bh))
            return success("Lưu lịch sử thành công.");
        return error("Lỗi lưu lịch sử.");
    }

    private static AuctionMessage handleGetBalance(AuctionMessage req) {
        String username = (String) req.getData();
        return success(DataStorage.getBalance(username));
    }

    private static AuctionMessage handleUpdateBalance(AuctionMessage req) {
        // data: Object[] {String username, double amountToChange}
        Object[] data = (Object[]) req.getData();
        String username    = (String) data[0];
        double amountDelta = (double) data[1];
        if (DataStorage.updateBalance(username, amountDelta))
            return success("Cập nhật số dư thành công.");
        return error("Lỗi cập nhật số dư.");
    }

    private static AuctionMessage handleExecutePayment(AuctionMessage req) {
        // data: Object[] {String username, String productId, double amount}
        Object[] data = (Object[]) req.getData();
        String username = (String) data[0];
        String pid      = (String) data[1];
        double amount   = (double) data[2];
        if (DataStorage.executeManualPayment(username, pid, amount))
            return success("Thanh toán thành công!");
        return error("Thanh toán thất bại.");
    }

    private static AuctionMessage handleGetMaxBid(AuctionMessage req) {
        // data: Object[] {String bidderName, String productId, double defaultPrice}
        Object[] data    = (Object[]) req.getData();
        String bidder    = (String) data[0];
        String pid       = (String) data[1];
        double defPrice  = (double) data[2];
        return success(DataStorage.getMaxBidByBidderForProduct(bidder, pid, defPrice));
    }

    private static AuctionMessage handleGetChartData(AuctionMessage req) {
        // XYChart.Series không Serializable – không thể gửi qua socket
        // Client tự gọi DataStorage.getProductChartData() local
        return error("Chart data phải được lấy local, không qua network.");
    }

    // ──────────────────────────────────────────────────────────────
    //  ADMIN
    // ──────────────────────────────────────────────────────────────

    private static AuctionMessage handleGetAllAccounts() {
        return success(DataStorage.getAllAccounts());
    }

    private static AuctionMessage handleSetAccountLocked(AuctionMessage req) {
        // data: Object[] {String username, boolean locked}
        Object[] data   = (Object[]) req.getData();
        String username = (String)  data[0];
        boolean locked  = (boolean) data[1];
        if (DataStorage.setAccountLocked(username, locked))
            return success(locked ? "Đã khóa tài khoản." : "Đã mở khóa tài khoản.");
        return error("Lỗi cập nhật trạng thái khóa tài khoản.");
    }

    private static AuctionMessage handleIsAccountExists(AuctionMessage req) {
        String identifier = (String) req.getData();
        return success(DataStorage.isAccountExists(identifier));
    }

    // ──────────────────────────────────────────────────────────────
    //  TIỆN ÍCH
    // ──────────────────────────────────────────────────────────────
    private static AuctionMessage success(Object data) {
        return new AuctionMessage(AuctionMessage.Action.SUCCESS, data);
    }

    private static AuctionMessage error(String message) {
        return new AuctionMessage(AuctionMessage.Action.ERROR, message);
    }
}