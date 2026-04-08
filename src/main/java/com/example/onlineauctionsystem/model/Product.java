package com.example.onlineauctionsystem.model;
import java.io.Serializable;

public class Product implements Serializable {
    private String id;
    private String name;
    private double currentPrice;
    private String sellerName;
    private String highestBidder; // Lưu tên người đang trả giá cao nhất

    public Product(String id, String name, double initialPrice, String sellerName) {
        this.id = id;
        this.name = name;
        this.currentPrice = initialPrice;
        this.sellerName = sellerName;
        this.highestBidder = "None";
    }

    // LOGIC QUAN TRỌNG: Xử lý trả giá
    public boolean placeBid(double newAmount, String bidderName) {
        if (newAmount > this.currentPrice) {
            this.currentPrice = newAmount;
            this.highestBidder = bidderName;
            return true;
        }
        return false;
    }

    // Getters & Setters
    public String getName() { return name; }
    public double getCurrentPrice() { return currentPrice; }
    public String getHighestBidder() { return highestBidder; }
    public String getId() { return id; }
}