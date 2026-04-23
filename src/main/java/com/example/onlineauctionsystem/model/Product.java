package com.example.onlineauctionsystem.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Product implements Serializable {
    private String id;
    private String name;
    private String description;
    private double initialPrice;
    private double currentPrice;
    private String sellerName;
    private String highestBidder;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public Product(String id, String name, String description, double initialPrice,
                   LocalDateTime startTime, LocalDateTime endTime, String sellerName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.initialPrice = initialPrice;
        this.currentPrice = initialPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerName = sellerName;
        this.highestBidder = "None";
        this.status = "OPEN";
    }

    // --- LOGIC ĐẤU GIÁ ---
    public synchronized boolean placeBid(double newAmount, String bidderName) {
        LocalDateTime now = LocalDateTime.now();
        if (!status.equals("RUNNING")) return false;
        if (now.isBefore(startTime) || now.isAfter(endTime)) return false;

        if (newAmount > currentPrice) {
            this.currentPrice = newAmount;
            this.highestBidder = bidderName;
            return true;
        }
        return false;
    }

    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (status.equals("CANCELED") || status.equals("PAID")) return;

        if (now.isBefore(startTime)) {
            status = "OPEN";
        } else if (now.isAfter(startTime) && now.isBefore(endTime)) {
            status = "RUNNING";
        } else if (now.isAfter(endTime)) {
            status = "FINISHED";
        }
    }

    // --- CÁC HÀM GETTER (Để MySQL lấy dữ liệu từ Java lưu xuống DB) ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getInitialPrice() { return initialPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public String getHighestBidder() { return highestBidder; }
    public String getSellerName() { return sellerName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getStatus() { return status; }

    // --- CÁC HÀM SETTER (Để MySQL nạp dữ liệu từ DB vào Java) ---
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }
    public void setStatus(String status) { this.status = status; }
}