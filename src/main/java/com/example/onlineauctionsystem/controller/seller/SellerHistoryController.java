package com.example.onlineauctionsystem.controller.seller;

import com.example.onlineauctionsystem.controller.MenuController;
import com.example.onlineauctionsystem.controller.common.ProductItemController;
import com.example.onlineauctionsystem.controller.common.ProductItemHistoryController;
import com.example.onlineauctionsystem.model.DataStorage;
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

public class SellerHistoryController extends MenuController {
    @FXML private Label lblSum, lblProductSell,lblRate;
    @FXML private VBox productListContainer;
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private ScheduledExecutorService scheduler; //tạo luồng chạy ngầm lặp lại theo lịch trình
    private  final int REFRESH_SECONDS = 5;

    @Override
    public void initialize() {
        productList.addListener((ListChangeListener<Product>) change -> {
            renderProductList();
            updateHeader();
        } );
        renderProductList();
        startAutoRefresh();
    }

    private List<Product> fetchMyProducts(){
        String seller_username = DataStorage.currentAccount.getUsername();
        return DataStorage.getAllProducts().stream()
                .filter(p -> seller_username.equals(p.getSellerName()))
                .filter(p -> "FINISHED".equals(p.getStatus())
                        || "PAID".equals(p.getStatus())
                        || "CANCELED".equals(p.getStatus()))
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
            List<Product> fresh = fetchMyProducts();
            Platform.runLater(() -> productList.setAll(fresh));
        }, 0, REFRESH_SECONDS, TimeUnit.SECONDS);
    }
    private  void stopAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void renderProductList(){
        productListContainer.getChildren().clear();

        if (productList.isEmpty()) {
            productListContainer.setPrefHeight(380);
            Label empty = new Label("Bạn chưa có sản phẩm nào đã bán");
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
                                SceneConfig.PRODUCT_ITEM_HISTORY.getPath()
                        )
                );
                Node row = loader.load();
                ProductItemHistoryController rowCtrl = loader.getController();
                rowCtrl.setData(p);
                productListContainer.getChildren().add(row);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateHeader(){
        double sumPrice = 0;
        long sumProduct = 0;
        for (Product p : productList){
            if("PAID".equals(p.getStatus())){
                sumPrice += p.getCurrentPrice();
                sumProduct += 1;
            }
        }
        lblSum.setText(formatPrice(sumPrice));
        lblProductSell.setText(String.valueOf(sumProduct));
        int total = productList.size();
        if (total > 0) {
            double rate = ((double) sumProduct / total) * 100;
            lblRate.setText(String.format("%.1f%%", rate)); // VD: 75.0%
        } else {
            lblRate.setText("0.0%");
        }
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
            DataStorage.currentAccount = null;
            switchScene(event, SceneConfig.LOGIN);

        }
    }
}
