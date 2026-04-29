package com.example.onlineauctionsystem.utils;

public class Validator {
    // 1. Kiểm tra định dạng Email
    public static boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(regex);
    }

    // 2. Kiểm tra CCCD đúng 12 số
    public static boolean isValidCCCD(String cccd) {
        return cccd != null && cccd.matches("\\d{12}");
    }

    // 3. Kiểm tra mật khẩu mạnh (8 ký tự, 1 chữ hoa, 1 ký tự đặc biệt)
    public static boolean isValidPassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$";
        return password != null && password.matches(regex);
    }

    // 4. Kiểm tra số dư lớn hơn hoặc bằng số tiền muốn trả
    public static boolean hasEnoughMoney(double balance, double bidAmount) {
        return balance >= bidAmount;
    }
}
