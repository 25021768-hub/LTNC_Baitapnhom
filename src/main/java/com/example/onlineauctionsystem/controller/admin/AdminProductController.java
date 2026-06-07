package com.example.onlineauctionsystem.controller.admin;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.controller.common.ProductPendingController;
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

public class AdminProductController extends BaseController {

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    @FXML private VBox productListContainer;
    private ScheduledExecutorService scheduler; // Tạo luồng chạy ngầm lặp lại theo lịch trình
    private final int REFRESH_SECONDS = 5;

    @Override
    public void initialize() {
        productList.addListener((ListChangeListener<Product>) change -> {
            renderProductList();
        });

        // Sửa lỗi chính tả biến và lấy danh sách sản phẩm chờ duyệt ban đầu
        List<Product> initialProducts = fetchAllProduct();
        productList.setAll(initialProducts);
        startAutoRefresh();
    }

    private List<Product> fetchAllProduct() {
        return RemoteDataStorage.getAllProducts().stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    private void renderProductList() {
        productListContainer.getChildren().clear();

        if (productList.isEmpty()) {
            productListContainer.setPrefHeight(380);
            Label empty = new Label("Chưa có sản phẩm nào yêu cầu phê duyệt");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #999999;");
            empty.setPrefWidth(770);
            empty.setPrefHeight(380);
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            productListContainer.getChildren().add(empty);
            return;
        }

        productListContainer.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        for (Product p : productList) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(SceneConfig.PRODUCT_PENDING.getPath())
                );
                Node row = loader.load();

                // Ép kiểu về đúng controller dòng sản phẩm
                ProductPendingController rowCtrl = loader.getController();


                rowCtrl.setData(p, this::handleProductApproval);

                productListContainer.getChildren().add(row);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleProductApproval(Product product, Boolean isApproved) {
        String targetStatus = isApproved ? "RUNNING" : "CANCELED";
        boolean dbUpdated = RemoteDataStorage.updateProductStatus(product.getId(), targetStatus);

        if (dbUpdated) {
            // Cập nhật trạng thái của đối tượng trên RAM cho đồng bộ
            product.setStatus(targetStatus);

            // Hiển thị thông báo thích hợp cho Admin
            if (isApproved) {
                showAlert("Thành công", "Đã phê duyệt sản phẩm: " + product.getName());
            } else {
                showAlert("Thông báo", "Đã từ chối sản phẩm: " + product.getName());
            }

            Platform.runLater(() -> {
                productList.removeIf(p -> p.getId().equals(product.getId()));
            });
        } else {
            showAlert("Lỗi", "Không thể cập nhật trạng thái sản phẩm xuống Database!");
        }
    }

    private void startAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "admin-product-refresh");
            t.setDaemon(true); // Biến thành luồng chạy nền
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            List<Product> fresh = fetchAllProduct();

            Platform.runLater(() -> {
                // Kiểm tra xem số lượng sản phẩm đang chờ duyệt có biến động gì không
                boolean isChanged = fresh.size() != productList.size();
                if (!isChanged) {
                    for (int i = 0; i < fresh.size(); i++) {
                        if (!fresh.get(i).getStatus().equals(productList.get(i).getStatus())) {
                            isChanged = true;
                            break;
                        }
                    }
                }

                // Chỉ thực hiện ghi đè dữ liệu để render lại màn hình khi phát hiện có thay đổi thực tế
                if (isChanged) {
                    productList.setAll(fresh);
                }
            });
        }, 0, REFRESH_SECONDS, TimeUnit.SECONDS);
    }

    private void stopAutoRefresh() {
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