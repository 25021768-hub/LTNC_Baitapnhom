package com.example.onlineauctionsystem.network;

import java.io.Serializable;

/**
 * Đối tượng "phong bì" dùng để truyền dữ liệu qua mạng giữa Client ↔ Server.
 * Phải implement Serializable để Java có thể chuyển thành byte stream qua Socket.
 *
 * Mỗi tin nhắn gồm:
 *   - action : cho Server/Client biết đây là yêu cầu/phản hồi loại gì
 *   - data   : dữ liệu đi kèm (có thể là String, Account, Product, List, Object[],...)
 */
public class AuctionMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    // ──────────────────────────────────────────────────────────────
    //  DANH SÁCH HÀNH ĐỘNG (phải khớp giữa Client và Server)
    // ──────────────────────────────────────────────────────────────
    public enum Action {
        // ── Auth ──
        LOGIN,                  // data: String[] {username, password}
        REGISTER,               // data: Account
        CHANGE_PASSWORD,        // data: String[] {username, oldPass, newPass}
        FORGOT_PASSWORD,        // data: String[] {identifier, newPass}
        UPDATE_ACCOUNT,         // data: Account

        // ── Sản phẩm ──
        GET_ALL_PRODUCTS,       // data: null  → trả về List<Product>
        GET_PRODUCT_BY_ID,      // data: String productId
        ADD_PRODUCT,            // data: Product
        DELETE_PRODUCT,         // data: Object[] {String productId, Account adminAcc}
        DELETE_MY_PRODUCT,      // data: Object[] {String productId, String sellerUsername}
        UPDATE_PRODUCT_STATUS,  // data: Object[] {String productId, String newStatus}

        // ── Đấu giá ──
        BID,                    // data: Object[] {String productId, double amount, String bidderUsername}
        SETUP_AUTO_BID,         // data: Object[] {String username, String productId, double maxPrice}
        TRIGGER_AUTO_BID,       // data: Object[] {String productId, double currentPrice, double bidIncrement}

        // ── Lịch sử & Số dư ──
        GET_BID_HISTORY,        // data: String bidderUsername → trả về List<BidHistory>
        GET_RUNNING_BIDS,       // data: String bidderUsername → trả về List<Product>
        SAVE_BID_HISTORY,       // data: BidHistory
        GET_BALANCE,            // data: String username → trả về Double
        UPDATE_BALANCE,         // data: Object[] {String username, double amountToChange}
        EXECUTE_PAYMENT,        // data: Object[] {String username, String productId, double amount}
        GET_MAX_BID,            // data: Object[] {String bidderName, String productId, double defaultPrice}
        GET_CHART_DATA,         // data: Object[] {String productId, String productName}

        // ── Admin ──
        GET_ALL_ACCOUNTS,       // data: null → trả về List<Account>
        SET_ACCOUNT_LOCKED,     // data: Object[] {String username, boolean locked}
        IS_ACCOUNT_EXISTS,      // data: String identifier → trả về Boolean

        // ── Kết quả ──
        SUCCESS,                // data: kết quả tuỳ action
        ERROR                   // data: String thông báo lỗi
    }

    private Action action;
    private Object data;

    public AuctionMessage(Action action, Object data) {
        this.action = action;
        this.data   = data;
    }

    public Action getAction() { return action; }
    public Object getData()   { return data;   }

    @Override
    public String toString() {
        return "AuctionMessage{action=" + action + ", data=" + data + "}";
    }
}