package com.example.onlineauctionsystem.model;
import java.io.Serializable;

public class Product implements Serializable {
    private String name;
    private double currentPrice;
    private String sellerName;

    public Product(String name, double currentPrice, String sellerName) {
        this.name = name;
        this.currentPrice = currentPrice;
        this.sellerName = sellerName;
    }

    public String getName() { return name; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
}