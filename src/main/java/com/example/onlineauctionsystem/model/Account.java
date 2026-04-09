package com.example.onlineauctionsystem.model;
import java.io.Serializable;

public abstract class Account implements Serializable {
    protected String username;
    protected String password;

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public abstract String getRole(); // Để phân biệt Bidder/Seller
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
