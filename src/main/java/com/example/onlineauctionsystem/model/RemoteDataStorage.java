package com.example.onlineauctionsystem.model;

import com.example.onlineauctionsystem.network.AuctionClient;
import com.example.onlineauctionsystem.network.AuctionMessage;
import javafx.scene.chart.XYChart;

import java.util.ArrayList;
import java.util.List;

import static com.example.onlineauctionsystem.network.AuctionMessage.Action.SUCCESS;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  RemoteDataStorage  – Thay thế DataStorage cho chế độ Network
 * ╠══════════════════════════════════════════════════════════════╣
 *
 *  Cách dùng:
 *    Mỗi Controller hiện đang import DataStorage. Chỉ cần đổi:
 *       *    thành:
 *      import com.example.onlineauctionsystem.model.RemoteDataStorage as DataStorage;
 *    (hoặc đổi tên file này thành DataStorage.java và thay file cũ)
 *
 *  Mọi method có cùng signature với DataStorage gốc.
 *  currentAccount vẫn là biến static public để tương thích.
 * ══════════════════════════════════════════════════════════════
 */
public class RemoteDataStorage {

    /** Tài khoản đang đăng nhập – lưu trên client, không qua server */
    public static Account currentAccount;
    public static String currentToken;
    public static boolean validateSession(String username, String token) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.VALIDATE_SESSION, new Object[]{username, token}
        ));
        return res.getAction() == AuctionMessage.Action.SUCCESS;
    }

    public static Account checkLogin(String username, String password) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.LOGIN, new String[]{username, password}));
        if (res.getAction() == SUCCESS) {
            Object[] payload = (Object[]) res.getData();
            currentAccount = (Account) payload[0];
            currentToken   = (String)  payload[1];
            return currentAccount;
        }
        return null;
    }

    // ──────────────────────────────────────────────────────────────
    //  AUTH
    // ──────────────────────────────────────────────────────────────


    public static boolean isAccountExists(String identifier) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.IS_ACCOUNT_EXISTS, identifier
        ));
        return res.getAction() == SUCCESS
                && Boolean.TRUE.equals(res.getData());
    }

    public static boolean register(Account acc) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.REGISTER, acc
        ));
        return res.getAction() == SUCCESS;
    }

    public static boolean changeForgotPassword(String identifier, String newPass) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.FORGOT_PASSWORD, new String[]{identifier, newPass}
        ));
        return res.getAction() == SUCCESS;
    }

    public static boolean updateAccount(Account acc) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.UPDATE_ACCOUNT, acc
        ));
        return res.getAction() == SUCCESS;
    }

    public static boolean changePassword(String username, String oldPass, String newPass) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.CHANGE_PASSWORD, new String[]{username, oldPass, newPass}
        ));
        return res.getAction() == SUCCESS;
    }

    // ──────────────────────────────────────────────────────────────
    //  SẢN PHẨM
    // ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static List<Product> getAllProducts() {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.GET_ALL_PRODUCTS, null
        ));
        return res.getAction() == SUCCESS
                ? (List<Product>) res.getData() : List.of();
    }

    public static Product findProductById(String id) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.GET_PRODUCT_BY_ID, id
        ));
        return res.getAction() == SUCCESS
                ? (Product) res.getData() : null;
    }

    public static boolean addProduct(Product p) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.ADD_PRODUCT, p
        ));
        return res.getAction() == SUCCESS;
    }

    public static boolean deleteProduct(String id, Account loggedInUser) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.DELETE_PRODUCT, new Object[]{id, loggedInUser}
        ));
        return res.getAction() == SUCCESS;
    }

    public static boolean deleteMyProduct(String productId, String sellerUsername) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.DELETE_MY_PRODUCT, new Object[]{productId, sellerUsername}
        ));
        return res.getAction() == SUCCESS;
    }

    public static boolean updateProductStatus(String productId, String newStatus) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.UPDATE_PRODUCT_STATUS, new Object[]{productId, newStatus}
        ));
        return res.getAction() == SUCCESS;
    }

    // ──────────────────────────────────────────────────────────────
    //  ĐẤU GIÁ
    // ──────────────────────────────────────────────────────────────

    public static boolean updateBid(String productId, double newPrice, String bidderName) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.BID, new Object[]{productId, newPrice, bidderName}
        ));
        return res.getAction() == SUCCESS;
    }

    public static boolean setupAutoBid(String username, String productId, double maxPrice) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.SETUP_AUTO_BID, new Object[]{username, productId, maxPrice}
        ));
        return res.getAction() == SUCCESS;
    }

    public static void triggerAutoBidSystem(String productId, double currentPrice, double bidIncrement) {
        AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.TRIGGER_AUTO_BID, new Object[]{productId, currentPrice, bidIncrement}
        ));
        // fire-and-forget: không cần xử lý kết quả
    }

    // ──────────────────────────────────────────────────────────────
    //  LỊCH SỬ & SỐ DƯ
    // ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static List<BidHistory> getBidHistory(String bidderName) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.GET_BID_HISTORY, bidderName
        ));
        return res.getAction() == SUCCESS
                ? (List<BidHistory>) res.getData() : List.of();
    }

    @SuppressWarnings("unchecked")
    public static List<Product> getRunningBidsByBidder(String bidderName) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.GET_RUNNING_BIDS, bidderName
        ));
        return res.getAction() == SUCCESS
                ? (List<Product>) res.getData() : List.of();
    }

    public static boolean saveBidHistory(BidHistory bidH) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.SAVE_BID_HISTORY, bidH
        ));
        return res.getAction() == SUCCESS;
    }

    public static double getBalance(String username) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.GET_BALANCE, username
        ));
        if (res.getAction() == SUCCESS && res.getData() instanceof Number n) {
            return n.doubleValue();
        }
        return 0.0;
    }

    public static boolean updateBalance(String username, double amountToChange) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.UPDATE_BALANCE, new Object[]{username, amountToChange}
        ));
        return res.getAction() == SUCCESS;
    }

    public static boolean executeManualPayment(String username, String productId, double amount) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.EXECUTE_PAYMENT, new Object[]{username, productId, amount}
        ));
        return res.getAction() == SUCCESS;
    }

    public static double getMaxBidByBidderForProduct(String bidderName, String productId, double defaultPrice) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.GET_MAX_BID, new Object[]{bidderName, productId, defaultPrice}
        ));
        if (res.getAction() == SUCCESS && res.getData() instanceof Number n) {
            return n.doubleValue();
        }
        return defaultPrice;
    }

    @SuppressWarnings("unchecked")
    public static XYChart.Series<String, Number> getProductChartData(String productId, String productName) {
        // Gửi request lên Server – nhận về List<String[]>{[time, price]}
        // Không gửi XYChart.Series qua socket vì nó không Serializable
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.GET_CHART_DATA, new Object[]{productId, productName}
        ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(productName);

        if (res.getAction() == SUCCESS && res.getData() instanceof Object[] payload) {
            // payload[0] = productName (String), payload[1] = List<String[]>
            java.util.List<String[]> points = (java.util.List<String[]>) payload[1];
            for (String[] point : points) {
                try {
                    String time  = point[0];
                    double price = Double.parseDouble(point[1]);
                    series.getData().add(new XYChart.Data<>(time, price));
                } catch (Exception ignored) {}
            }
        }
        return series;
    }

    // ──────────────────────────────────────────────────────────────
    //  ADMIN
    // ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static List<Account> getAllAccounts() {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.GET_ALL_ACCOUNTS, null
        ));
        return res.getAction() == SUCCESS
                ? (List<Account>) res.getData() : List.of();
    }

    public static boolean setAccountLocked(String username, boolean locked) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.SET_ACCOUNT_LOCKED, new Object[]{username, locked}
        ));
        return res.getAction() == SUCCESS;
    }

    // ──────────────────────────────────────────────────────────────
    //  MAINTENANCE (gọi server thực hiện, không làm local)
    // ──────────────────────────────────────────────────────────────

    /**
     * Server đã tự chạy autoClose mỗi 30 giây.
     * Client gọi hàm này trong initialize() → server xử lý rồi trả về danh sách đã cập nhật.
     * Không cần làm gì thêm ở client.
     */
    public static void autoCloseAndSaveExpiredProducts() {
        // Server đã tự chạy autoClose mỗi 30 giây qua ScheduledExecutorService.
        // Client KHÔNG cần gọi gì thêm – không spam network.
    }

    public static Account findAccountByUsername(String username) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.GET_ACCOUNT, username
        ));
        return res.getAction() == SUCCESS
                ? (Account) res.getData() : null;
    }
    public static boolean uploadImage(String fileName, byte[] imageBytes) {
        AuctionMessage res = AuctionClient.send(new AuctionMessage(
                AuctionMessage.Action.UPLOAD_IMAGE, new Object[]{fileName, imageBytes}
        ));
        return res.getAction() == AuctionMessage.Action.SUCCESS;
    }
}