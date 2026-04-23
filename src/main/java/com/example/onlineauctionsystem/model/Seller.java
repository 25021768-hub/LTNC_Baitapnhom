package com.example.onlineauctionsystem.model;

public class Seller extends Account {
    public Seller(String username, String password) {
        super(username, password, "SELLER");
    }

    @Override
    public String getRole() { return "SELLER"; }
}