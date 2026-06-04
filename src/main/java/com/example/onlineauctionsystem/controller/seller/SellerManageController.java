package com.example.onlineauctionsystem.controller.seller;
import com.example.onlineauctionsystem.controller.MenuController;
import com.example.onlineauctionsystem.controller.common.ProductCardController;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.ProductImage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SellerManageController extends MenuController {

    @FXML private FlowPane pendingContainer;

    private ScheduledExecutorService scheduler;
    private final int REFRESH_SECONDS = 5;

    @Override
    public void initialize() {
        startAutoRefresh();
    }

    private List<Product> fetchMyPendingProducts() {
        String me = DataStorage.currentAccount.getUsername();
        return DataStorage.getAllProducts().stream()
                .filter(p -> me.equals(p.getSellerName()))
                .filter(p -> "PENDING".equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    private void startAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "manage-refresh");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            List<Product> fresh = fetchMyPendingProducts();
            Platform.runLater(() -> renderCards(fresh));
        }, 0, REFRESH_SECONDS, TimeUnit.SECONDS);
    }

    public void stopAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void renderCards(List<Product> products) {
        pendingContainer.getChildren().clear();
        for (Product p : products) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(SceneConfig.PRODUCT_CARD.getPath())
                );
                Node card = loader.load();
                ProductCardController ctrl = loader.getController();
                ctrl.setData(p, this::onEditProduct, this::onDeleteProduct);
                pendingContainer.getChildren().add(card);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void onAddProduct(ActionEvent event) {
        if (DataStorage.currentAccount.isLocked()) {
            showAlert("Lỗi", "Tài khoản của bạn đã bị khóa! Không thể thực hiện chức năng này.");
            stopAutoRefresh();
            // Ép đăng xuất ngay lập tức
            forceLogout(event);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(SceneConfig.ADD_PRODUCT.getPath())
            );
            Parent root = loader.load();
            AddProductController ctrl = loader.getController();
            ctrl.setOnSuccessCallback(() -> {
                List<Product> fresh = fetchMyPendingProducts();
                Platform.runLater(() -> renderCards(fresh));
            });

            Stage popup = new Stage();
            popup.setTitle("Đăng bán sản phẩm mới");
            popup.setScene(new Scene(root));
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(pendingContainer.getScene().getWindow());
            popup.setResizable(false);
            popup.showAndWait();

        } catch (IOException e) { e.printStackTrace(); }
    }

    private void onEditProduct(Product p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(SceneConfig.ADD_PRODUCT.getPath())
            );
            Parent root = loader.load();
            AddProductController ctrl = loader.getController();
            ctrl.setOnSuccessCallback(() -> {
                List<Product> fresh = fetchMyPendingProducts();
                Platform.runLater(() -> renderCards(fresh));
            });

            Stage popup = new Stage();
            popup.setTitle("Sửa sản phẩm");
            popup.setScene(new Scene(root));
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(pendingContainer.getScene().getWindow());
            popup.setResizable(false);
            popup.showAndWait();

        } catch (IOException e) { e.printStackTrace(); }
    }

    private void onDeleteProduct(Product p) {
        boolean ok = DataStorage.deleteMyProduct(
                p.getId(),
                DataStorage.currentAccount.getUsername()
        );
        if (ok) {
            ProductImage.delete(p.getImagePath()); // xóa file ảnh luôn
            renderCards(fetchMyPendingProducts());
        } else {
            showAlert("Xóa thất bại", "Chỉ có thể xóa sản phẩm đang chờ duyệt!");
        }
    }

    // ── Navbar ─────────────────────────────────────────────────────
    @FXML
    @Override
    public void onMyProducts(ActionEvent event) {
        super.onMyProducts(event);
        stopAutoRefresh();
    }

    @FXML
    @Override
    public void onHistory(ActionEvent event) {
        super.onHistory(event);
        stopAutoRefresh();
    }

    @FXML
    @Override
    public void onManage(ActionEvent event) {
        super.onManage(event);
        stopAutoRefresh();
    }

    @FXML
    @Override
    public void onAccount(ActionEvent event) {
        super.onAccount(event);
        stopAutoRefresh();
    }

    @Override
    @FXML
    public void onLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            stopAutoRefresh();
            DataStorage.currentAccount = null;
            switchScene(event, SceneConfig.LOGIN);
        }
    }
}