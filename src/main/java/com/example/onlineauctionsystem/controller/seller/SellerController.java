package com.example.onlineauctionsystem.controller.seller;

import com.example.onlineauctionsystem.controller.MenuController;
import com.example.onlineauctionsystem.controller.common.ProductItemController;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.model.Product;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
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
    private ScheduledExecutorService scheduler; //tạo luồng chạy ngầm lặp lại theo lịch trình
    private  final int REFRESH_SECONDS = 5;
    @Override
    public void initialize(){
        productList.addListener((ListChangeListener<Product>) change -> {
            renderProductList();
            updateFooter();
        } );
        startAutoRefresh();
    }
    private List<Product> fetchMyProducts(){
        String seller_username = DataStorage.currentAccount.getUsername();
        return DataStorage.getAllProducts().stream()
                .filter(p -> seller_username.equals(p.getSellerName()))
                .filter(p -> "OPEN".equals(p.getStatus())
                        || "RUNNING".equals(p.getStatus()))
                .collect(Collectors.toList());
    }
    private void startAutoRefresh(){
        if(scheduler != null || !scheduler.isShutdown()) return;
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
        if (scheduler != null || !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }
    private void renderProductList(){
        productListContainer.getChildren().clear();

        if (productList.isEmpty()) {
            Label empty = new Label("Bạn chưa có sản phẩm nào đang bán");
            empty.setStyle("-fx-font-size: 16; -fx-text-fill: #999999;");
            empty.setPrefHeight(200);
            productListContainer.getChildren().add(empty);
            return;
        }

        for (Product p : productList) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                "/com/example/onlineauctionsystem/ProductItem.fxml"
                        )
                );
                Node row = loader.load();
                ProductItemController rowCtrl = loader.getController();
                rowCtrl.setData(p);
                productListContainer.getChildren().add(row);

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

    private String formatPrice(double price) {
        return String.format("%,.0fđ", price).replace(",", ".");
    }

    @FXML
    @Override
    public void onMyProducts(ActionEvent event) {
        super.onMyProducts(event);
    }

    @FXML
    @Override
    public void onHistory(ActionEvent event) {
        super.onHistory(event);
    }

    @FXML
    @Override
    public void onManage(ActionEvent event) {
        super.onManage(event);
    }

    @FXML
    @Override
    public void onAccount(ActionEvent event) {
        super.onAccount(event);
    }
}