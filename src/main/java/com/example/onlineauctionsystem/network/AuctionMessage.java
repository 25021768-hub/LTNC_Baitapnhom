package com.example.onlineauctionsystem.network;

import java.io.Serializable;

public class AuctionMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Action {
        // ── Auth ──
        LOGIN,
        REGISTER,
        CHANGE_PASSWORD,
        FORGOT_PASSWORD,
        UPDATE_ACCOUNT,
        VALIDATE_SESSION,
        LOGOUT,

        // ── Sản phẩm ──
        GET_ALL_PRODUCTS,
        GET_PRODUCT_BY_ID,
        ADD_PRODUCT,
        DELETE_PRODUCT,
        DELETE_MY_PRODUCT,
        UPDATE_PRODUCT_STATUS,

        // ── Đấu giá ──
        BID,
        SETUP_AUTO_BID,
        TRIGGER_AUTO_BID,

        // ── Lịch sử & Số dư ──
        GET_BID_HISTORY,
        GET_RUNNING_BIDS,
        SAVE_BID_HISTORY,
        GET_BALANCE,
        UPDATE_BALANCE,
        EXECUTE_PAYMENT,
        GET_MAX_BID,
        GET_CHART_DATA,
        GET_IMAGE,
        UPLOAD_IMAGE,

        // ── Admin ──
        GET_ALL_ACCOUNTS,
        SET_ACCOUNT_LOCKED,
        IS_ACCOUNT_EXISTS,
        GET_ACCOUNT,

        // ── Kết quả ──
        SUCCESS,
        ERROR,
        ACCOUNT_IN_USE
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