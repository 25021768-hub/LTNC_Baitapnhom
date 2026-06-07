package com.example.onlineauctionsystem.controller.seller;

import com.example.onlineauctionsystem.controller.MenuController;
import com.example.onlineauctionsystem.controller.common.ProductItemController;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SellerController extends MenuController {
    @FXML private VBox productListContainer;
    @FXML private Label lblTotalProducts, lblTotalRevenue;
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final List<ProductItemController> cardControllers = new ArrayList<>();
    private ScheduledExecutorService scheduler; //tạo luồng chạy ngầm lặp lại theo lịch trình
    private int refreshCounter = 0;
    private final int DATA_REFRESH_INTERVAL = 5;
    @Override
    public void initialize(){
        productList.addListener((ListChangeListener<Product>) change -> {
            renderProductList();
            updateFooter();
        } );
        renderProductList();
        updateFooter();
        startAutoRefresh();
    }
    private List<Product> fetchMyProducts(){
        String seller_username = RemoteDataStorage.currentAccount.getUsername();
        return RemoteDataStorage.getAllProducts().stream()
                .filter(p -> seller_username.equals(p.getSellerName()))
                // BUG I: Filter cũ bỏ sót PENDING — SP mới tạo sẽ không hiện ở "Sản phẩm của tôi"
                // PENDING hiển thị ở tab "Quản lý đăng bán", OPEN+RUNNING hiển thị ở "SP của tôi"
                // Giữ nguyên logic domain: trang này chỉ hiện SP đang hoạt động
                .filter(p -> "OPEN".equals(p.getStatus())
                        || "RUNNING".equals(p.getStatus())
                        || "PENDING".equals(p.getStatus())) // THÊM PENDING để seller thấy SP chờ duyệt
                .collect(Collectors.toList());
    }
    private void startAutoRefresh(){
        if(scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "seller-refresh");
            t.setDaemon(true); //biến thành luồng chạy nền
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            refreshCounter++;

            if (refreshCounter >= DATA_REFRESH_INTERVAL) {
                refreshCounter = 0;
                List<Product> fresh = fetchMyProducts();
                Platform.runLater(() -> productList.setAll(fresh));
            } else {
                // Mỗi giây cập nhật đồng hồ
                Platform.runLater(() -> {
                    for (ProductItemController ctrl : cardControllers) {
                        ctrl.updateTime();
                    }
                });
            }

        }, 0, 1, TimeUnit.SECONDS);

    }
    private  void stopAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }
    private void renderProductList(){
        productListContainer.getChildren().clear();
        cardControllers.clear();
        if (productList.isEmpty()) {
            productListContainer.setPrefHeight(380);
            Label empty = new Label("Bạn chưa có sản phẩm nào đang bán");
            empty.setStyle("-fx-font-size: 16; -fx-text-fill: #999999;");
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
                        getClass().getResource(
                                SceneConfig.PRODUCT_ITEM.getPath()
                        )
                );
                Node row = loader.load();
                ProductItemController rowCtrl = loader.getController();
                rowCtrl.setData(p);
                productListContainer.getChildren().add(row);
                cardControllers.add(rowCtrl);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void updateFooter() {
        lblTotalProducts.setText(String.valueOf(productList.size()));

        double total = productList.stream()
                .mapToDouble(Product::getCurrentPrice)
                .sum();
        lblTotalRevenue.setText(formatPrice(total));
    }

    @FXML
    @Override
    public void onMyProducts(ActionEvent event) {
        stopAutoRefresh();
        super.onMyProducts(event);

    }

    @FXML
    @Override
    public void onHistory(ActionEvent event) {
        stopAutoRefresh();
        super.onHistory(event);

    }

    @FXML
    @Override
    public void onManage(ActionEvent event) {
        stopAutoRefresh();
        super.onManage(event);

    }

    @FXML
    @Override
    public void onAccount(ActionEvent event) {
        stopAutoRefresh();
        super.onAccount(event);

    }

    @Override
    @FXML
    public void onLogout(ActionEvent event) {
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
}