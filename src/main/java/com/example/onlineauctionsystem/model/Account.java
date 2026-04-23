package com.example.onlineauctionsystem.model;

import java.io.Serializable;

public class Account implements Serializable {
    private String username;
    private String password;
    private String role; // "ADMIN", "SELLER", "BIDDER"

    public Account(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    // BẮT BUỘC PHẢI CÓ HÀM NÀY ĐỂ ĐỔI MẬT KHẨU
    public void setPassword(String password) {
        this.password = password;
    }
}