package com.example.onlineauctionsystem.controller.bidder;

import com.example.onlineauctionsystem.controller.MenuController;
import com.example.onlineauctionsystem.controller.common.BidNewProductCardController;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BidderController extends MenuController {
    @FXML private GridPane productContainer;
    @FXML private TextField txtSearch;
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final List<BidNewProductCardController> cardControllers = new ArrayList<>();
    private int refreshCounter = 0;
    private final int DATA_REFRESH_INTERVAL = 5;
    private ScheduledExecutorService scheduler;

    public void initialize() {
        productList.addListener((ListChangeListener<Product>) change -> {
            applySearchFilter();
        });
        List<Product> initialData = fetchProducts();
        renderProductList(initialData);
        startAutoRefresh();
    }

    private List<Product> fetchProducts(){
        return RemoteDataStorage.getAllProducts().stream()
                .filter(p -> "RUNNING".equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    private void startAutoRefresh(){
        if(scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "bidder-refresh");
            t.setDaemon(true); // biến thành luồng chạy nền
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            refreshCounter++;
            if (refreshCounter >= DATA_REFRESH_INTERVAL) {
                refreshCounter = 0;
                List<Product> fresh = fetchProducts();
                Platform.runLater(() -> productList.setAll(fresh));
            } else {
                // Mỗi giây cập nhật đồng hồ
                Platform.runLater(() -> {
                    for (BidNewProductCardController ctrl : cardControllers) {
                        ctrl.updateTime();
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void stopAutoRefresh(){
        if(scheduler != null && !scheduler.isShutdown()){
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    @FXML
    private void renderProductList(List<Product> products){
        productContainer.getChildren().clear();
        productContainer.getRowConstraints().clear();
        cardControllers.clear();
        if (products == null || products.isEmpty()) {
            productContainer.setPrefHeight(380);
            Label empty = new Label("Chưa có sản phẩm nào đang bán");
            empty.setStyle("-fx-font-size: 16; -fx-text-fill: #999999;");
            GridPane.setColumnSpan(empty, 2);
            empty.setPrefWidth(860);
            empty.setPrefHeight(380);
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            productContainer.add(empty, 0, 0);
            return;
        }

        productContainer.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        int column = 0;
        int row = 0;
        for (Product p : products) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                SceneConfig.BID_NEW_PRODUCT.getPath()
                        )
                );
                Node rowNode = loader.load();
                GridPane.setHgrow(rowNode, Priority.ALWAYS);
                BidNewProductCardController rowCtrl = loader.getController();
                rowCtrl.setData(p, this::onBidProduct);
                productContainer.add(rowNode, column, row);
                cardControllers.add(rowCtrl);

                column++;
                if (column > 1) {
                    column = 0;
                    row++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onBidProduct(Product p){
        stopAutoRefresh();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(SceneConfig.BIDDER_WINDOW.getPath())
            );
            Parent root = loader.load();

            // Truyền product sang trang đấu giá
            AuctionController ctrl = loader.getController();
            ctrl.setProduct(p);

            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(SceneConfig.BIDDER_WINDOW.getTitle());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch(KeyEvent keyEvent) {
        applySearchFilter();
    }

    private void applySearchFilter() {
        if (txtSearch == null) {
            renderProductList(productList);
            return;
        }

        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            renderProductList(productList);
        } else {
            List<Product> filtered = productList.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
            renderProductList(filtered);
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
            RemoteDataStorage.currentAccount = null;
            RemoteDataStorage.currentToken = null;
            switchScene(event, SceneConfig.LOGIN);
        }
    }
}