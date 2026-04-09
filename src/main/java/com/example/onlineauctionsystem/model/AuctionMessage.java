package com.example.onlineauctionsystem.model;
import java.io.Serializable;

public class AuctionMessage implements Serializable {
    public enum Action { LOGIN, REGISTER, BID, UPDATE_LIST, ADD_PRODUCT, DELETE_PRODUCT, ERROR, SUCCESS }

    private Action action;
    private Object data;

    public AuctionMessage(Action action, Object data) {
        this.action = action;
        this.data = data;
    }

    public Action getAction() { return action; }
    public Object getData() { return data; }
}