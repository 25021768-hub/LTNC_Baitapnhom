package com.example.onlineauctionsystem.model;

public class Bidder extends Account {
    private final double balance;

    public Bidder(String username, String password, double balance) {
        super(username, password,"BIDDER");
        this.balance = balance;
    }

    @Override
    public String getRole() { return "BIDDER"; }
    public double getBalance() { return balance; }
}