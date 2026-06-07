package com.example.onlineauctionsystem.controller.admin;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.controller.common.ProductItemController;
import com.example.onlineauctionsystem.controller.common.UserController;
import com.example.onlineauctionsystem.model.Account;
import com.example.onlineauctionsystem.model.RemoteDataStorage;
import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AdminUserController extends BaseController {
    @FXML
    private VBox userListContainer;
    private final ObservableList<Account> accountList = FXCollections.observableArrayList();
    private ScheduledExecutorService scheduler; //tạo luồng chạy ngầm lặp lại theo lịch trình
    private final int REFRESH_SECONDS = 5;

    @Override
    public void initialize() {
        accountList.addListener((ListChangeListener<Account>) change -> {
            renderUserList();
        });
        List<Account> initialAccounts = fetchMyAccount();
        accountList.setAll(initialAccounts);
        startAutoRefresh();
    }

    private List<Account> fetchMyAccount() {
        return RemoteDataStorage.getAllAccounts().stream()
                .filter(a -> !"ADMIN".equals(a.getRole()))
                .collect(Collectors.toList());
    }

    private void renderUserList() {
        userListContainer.getChildren().clear();

        if (accountList.isEmpty()) {
            userListContainer.setPrefHeight(380);
            Label empty = new Label("Chưa có người dùng nào");
            empty.setStyle("-fx-font-size: 16; -fx-text-fill: #999999;");
            empty.setPrefWidth(770);
            empty.setPrefHeight(380);
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            userListContainer.getChildren().add(empty);
            return;
        }
        userListContainer.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        for (Account a : accountList) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                SceneConfig.USER.getPath()
                        )
                );
                Node row = loader.load();
                UserController rowCtrl = loader.getController();
                rowCtrl.setData(a, this::onToggleLock);
                userListContainer.getChildren().add(row);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onToggleLock(Account acc) {
        boolean newLocked = !acc.isLocked();
        boolean ok = RemoteDataStorage.setAccountLocked(acc.getUsername(), newLocked);
        if (ok) {
            acc.setLocked(newLocked);
            List<Account> fresh = fetchMyAccount();
            accountList.setAll(fresh);
        } else {
            showAlert("Lỗi", "Không thể thay đổi trạng thái tài khoản!");
        }
    }

    private void startAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "admin-user-refresh");
            t.setDaemon(true); //biến thành luồng chạy nền
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            List<Account> fresh = fetchMyAccount();

            Platform.runLater(() -> {
                // Kiểm tra xem dữ liệu mới lấy về từ Database có bất kỳ sự thay đổi nào so với UI hiện tại không
                boolean isChanged = fresh.size() != accountList.size();
                if (!isChanged) {
                    for (int i = 0; i < fresh.size(); i++) {
                        if (fresh.get(i).isLocked() != accountList.get(i).isLocked()) {
                            isChanged = true;
                            break;
                        }
                    }
                }

                // Chỉ thực hiện ghi đè dữ liệu vẽ lại màn hình khi phát hiện có sự thay đổi thực sự
                if (isChanged) {
                    accountList.setAll(fresh);
                }
            });
        }, 0, REFRESH_SECONDS, TimeUnit.SECONDS);
    }
    private  void stopAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    @FXML
    private void onLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            stopAutoRefresh();
            RemoteDataStorage.currentAccount = null;
            RemoteDataStorage.currentToken = null;
            switchScene(event, SceneConfig.LOGIN);

        }
    }
    @FXML
    private void onProductPending(ActionEvent event) {
        stopAutoRefresh();
        switchScene(event, SceneConfig.ADMIN_PRODUCT);
    }
    @FXML
    private void onAdminSystem(ActionEvent event) {
        stopAutoRefresh();
        switchScene(event, SceneConfig.ADMIN_USER);
    }
}