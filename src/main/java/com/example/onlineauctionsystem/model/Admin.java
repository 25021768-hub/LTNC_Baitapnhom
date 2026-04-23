package com.example.onlineauctionsystem.model;

public class Admin extends Account {
    public Admin(String username, String password) {
        super(username, password, "ADMIN");
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
