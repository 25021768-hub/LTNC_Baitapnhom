package com.example.onlineauctionsystem.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Product implements Serializable {
    private String id;
    private String name;
    private String description; // Thêm mô tả
    private double initialPrice;
    private double currentPrice;
    private String sellerName;
    private String highestBidder;

    private LocalDateTime startTime; // Thời gian bắt đầu
    private LocalDateTime endTime;   // Thời gian kết thúc
    private String status;           // OPEN, RUNNING, FINISHED, PAID, CANCELED

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

    // LOGIC 3.1.3: Kiểm tra tính hợp lệ khi trả giá
    public synchronized boolean placeBid(double newAmount, String bidderName) {
        LocalDateTime now = LocalDateTime.now();

        // Chỉ cho phép trả giá khi đang trong phiên (RUNNING)
        if (!status.equals("RUNNING")) return false;

        // Kiểm tra thời gian thực tế
        if (now.isBefore(startTime) || now.isAfter(endTime)) return false;

        // Giá mới phải cao hơn giá hiện tại
        if (newAmount > currentPrice) {
            this.currentPrice = newAmount;
            this.highestBidder = bidderName;
            return true;
        }
        return false;
    }

    // LOGIC 3.1.4: Tự động cập nhật trạng thái dựa trên thời gian
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

    // Getter & Setter cần thiết cho việc Sửa/Xóa
    public String getId() { return id; }
    public String getStatus() { return status; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public double getCurrentPrice() { return currentPrice; }
    public String getHighestBidder() { return highestBidder; }; }