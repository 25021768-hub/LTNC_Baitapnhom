package com.example.onlineauctionsystem.model;

import java.io.Serializable;

// Vẫn giữ lại Serializable cho an toàn nếu có code cũ nào đó đang gọi đến
public class Account implements Serializable {
    private String username;
    private String password;
    private String role; // "ADMIN", "SELLER", "BIDDER"

    // THÊM 3 TRƯỜNG MỚI CHO GIAO DIỆN ĐĂNG KÝ
    private String idCard;
    private String email;
    private String phoneNumber;

    //So du
    private double balance;

    // 1. Constructor ĐẦY ĐỦ dùng cho lúc ĐĂNG KÝ
    public Account(String username, String password, String role, String idCard, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.idCard = idCard;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // 2. Constructor RÚT GỌN dùng cho lúc ĐĂNG NHẬP
    public Account(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // GETTER (Để lấy thông tin)
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getIdCard() { return idCard; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public double getBalance() { return balance; }

    // SETTER BẮT BUỘC PHẢI CÓ ĐỂ ĐỔI MẬT KHẨU (Của Hiếu)
    public void setPassword(String password) {
        this.password = password;
    }

    //Setter cho so du
    public void setBalance(double balance) {
        this.balance = balance;
    }
}