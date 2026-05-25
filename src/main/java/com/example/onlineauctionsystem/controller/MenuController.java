package com.example.onlineauctionsystem.controller;

import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.HashMap;
import java.util.Map;

public class MenuController extends BaseController {

    // 1. Tạo các bảng định tuyến (Routing Maps) cố định cho từng nút bấm
    protected static final Map<String, SceneConfig> myProductsRoutes = new HashMap<>();
    protected static final Map<String, SceneConfig> historyRoutes = new HashMap<>();
    protected static final Map<String, SceneConfig> manageRoutes = new HashMap<>();
    protected static final Map<String, SceneConfig> accountRoutes = new HashMap<>();

    // 2. Khối static nạp cấu hình đường đi duy nhất một lần khi ứng dụng chạy
    static {
        // Định tuyến cho nút "Sản phẩm của tôi"
        myProductsRoutes.put("SELLER", SceneConfig.SELLER_PRODUCT);
        myProductsRoutes.put("BIDDER", SceneConfig.BIDDER_PRODUCT);

        // Định tuyến cho nút "Lịch sử hàng đã bán/mua"
        historyRoutes.put("SELLER", SceneConfig.SELLER_HISTORY);
        historyRoutes.put("BIDDER", SceneConfig.BIDDER_HISTORY);

        // Định tuyến cho nút "Quản lý đăng bán"
        manageRoutes.put("SELLER", SceneConfig.SELLER_MANAGER);
        manageRoutes.put("BIDDER", SceneConfig.BIDDER_MANAGER);

        // Định tuyến cho nút "Tài khoản"
        accountRoutes.put("SELLER", SceneConfig.SELLER_HOME);
        accountRoutes.put("BIDDER", SceneConfig.BIDDER_HOME);
    }

    // Hàm bổ trợ lấy và chuẩn hóa Role, bọc lót an toàn tránh lỗi NullPointerException
    private String getCurrentRole() {
        if (DataStorage.currentAccount == null || DataStorage.currentAccount.getRole() == null) {
            return "GUEST";
        }
        return DataStorage.currentAccount.getRole().toUpperCase().trim();
    }

    // Hàm điều hướng dùng chung dựa trên bảng tra cứu Map
    private void navigateByRole(ActionEvent event, Map<String, SceneConfig> routes) {
        String role = getCurrentRole();
        SceneConfig targetScene = routes.get(role);

        if (targetScene != null) {
            switchScene(event, targetScene);
        } else {
            showAlert("Quyền truy cập", "Tài khoản của bạn không có quyền thực hiện chức năng này.");
        }
    }

    // 3. CÁC HÀM SỰ KIỆN GIAO DIỆN
    @FXML
    public void onMyProducts(ActionEvent event) {
        navigateByRole(event, myProductsRoutes);
    }

    @FXML
    public void onHistory(ActionEvent event) {
        navigateByRole(event, historyRoutes);
    }

    @FXML
    public void onManage(ActionEvent event) {
        navigateByRole(event, manageRoutes);
    }

    @FXML
    public void onAccount(ActionEvent event) {
        navigateByRole(event, accountRoutes);
    }

    @FXML
    public void onLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            DataStorage.currentAccount = null;
            switchScene(event, SceneConfig.LOGIN);
        }
    }

    @Override
    public void initialize() {

    }
}