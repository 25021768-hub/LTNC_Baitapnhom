package com.example.onlineauctionsystem.model;

import java.io.Serializable;

import java.time.LocalDateTime;

public class BidHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    private String productId;
    private String productName;
    private String bidderName;
    private double myBidPrice;
    private double finalPrice;
    private LocalDateTime endTime;
    private String result; // "WIN" hoặc "LOSE"
    private boolean isPaid = false;

    public BidHistory(String productId, String productName, String bidderName, double myBidPrice,
                      double finalPrice, LocalDateTime endTime, String result, boolean isPaid) {
        this.productId = productId;
        this.productName = productName;
        this.bidderName = bidderName;
        this.myBidPrice = myBidPrice;
        this.finalPrice = finalPrice;
        this.endTime = endTime;
        this.result = result;
        this.isPaid = isPaid;
    }
    public BidHistory(){}

    // Getters & Setters
    public String getBidderName(){return bidderName;}

    public void setBidderName(String BidderName){this.bidderName = BidderName;}

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