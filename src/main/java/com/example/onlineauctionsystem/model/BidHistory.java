package com.example.onlineauctionsystem.model;

import java.time.LocalDateTime;

public class BidHistory {
    private String productId;
    private String productName;
    private double myBidPrice;
    private double finalPrice;
    private LocalDateTime endTime;
    private String result; // "WIN" hoặc "LOSE"
    private boolean isPaid = false;

    // Getters & Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getMyBidPrice() {
        return myBidPrice;
    }

    public void setMyBidPrice(double myBidPrice) {
        this.myBidPrice = myBidPrice;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setPaid(boolean paid) {
        this.isPaid = paid;
    }
    public boolean getPaid(){
        return this.isPaid;
    }
}