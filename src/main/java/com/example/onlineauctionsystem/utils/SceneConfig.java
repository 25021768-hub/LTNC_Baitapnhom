package com.example.onlineauctionsystem.utils;

public enum SceneConfig {
    LOGIN("Dang_Nhap_BTL.fxml", "Đăng nhập"),
    REGISTER("Dang_Ky.fxml", "Đăng ký tài khoản"),
    HOME("Tai_Khoan.fxml", "Trang chủ người đấu giá"),
    FORGOT_PASSWORD("Quen_Mat_Khau_BTL.fxml", "Quên mật khẩu"),
    CHANGE_PASSWORD("Doi_Mat_Khau_BTL.fxml", "Đổi mật khẩu");

    private final String fileName;
    private final String title;
    private static final String FOLDER = "/com/example/onlineauctionsystem/";

    SceneConfig(String fileName, String title) {
        this.fileName = fileName;
        this.title = title;
    }

    public String getPath() { return FOLDER + fileName; }
    public String getTitle() { return title; }

}
