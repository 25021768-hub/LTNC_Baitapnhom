package com.example.onlineauctionsystem.utils;

public enum SceneConfig {
    LOGIN("Dang_Nhap_BTL.fxml", "Đăng nhập"),
    REGISTER("Dang_Ky.fxml", "Đăng ký tài khoản"),
    FORGOT_PASSWORD("Quen_Mat_Khau_BTL.fxml", "Quên mật khẩu"),
    CHANGE_PASSWORD("Doi_Mat_Khau_BTL.fxml", "Đổi mật khẩu"),

    BIDDER_HOME("Tai_Khoan.fxml", "Trang chủ người đấu giá"),
    BIDDER_HISTORY("Lich_Su_Dau_Gia_Bidder_BTL.fxml", "Lịch sử đấu giá"),
    BIDDER_PRODUCT("San_Pham_Moi_BIDDER_BTL.fxml", "Sản phẩm mới"),
    BIDDER_MANAGER("Ban_Dang_Dau_Gia_Bidder_BTL.fxml", "Sản phẩm đang đấu giá"),

    SELLER_HOME("Tai_Khoan_Seller_BTL.fxml","Trang chủ người bán"),
    SELLER_HISTORY("lich_Su_Hang_Da_Ban_Seller_BTL.fxml", "Sản phẩm đã bán"),
    SELLER_PRODUCT("San_Pham_Cua_Toi_Seller_BTL.fxml", "Sản phẩm đang bán"),
    SELLER_MANAGER("Quan_Ly_Dang_Ban_Seller_BTL.fxml", "Sản phầm của tôi"),

    PRODUCT_CARD("ProductCard.fxml", "Ô sản phẩm"),
    ADD_PRODUCT("Cua_So_Dang_Ban_San_Pham_Seller_BTL.fxml", "Cửa số đăng bán sản phẩm"),
    PRODUCT_ITEM("ProductItem.fxml", "Ô sản phảm đang đấu giá"),
    PRODUCT_ITEM_HISTORY("ProductItemHistory.fxml", "Ô sản phẩm lịch sử");

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
