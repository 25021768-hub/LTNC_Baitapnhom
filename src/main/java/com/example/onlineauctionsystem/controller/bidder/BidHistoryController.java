package com.example.onlineauctionsystem.controller.bidder;

import com.example.onlineauctionsystem.controller.MenuController;
import com.example.onlineauctionsystem.controller.common.BidderHistoryRowController;
import com.example.onlineauctionsystem.model.BidHistory;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BidHistoryController extends MenuController {

    @FXML
    private VBox historyContainer;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbFilter;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblWin;
    @FXML
    private Label lblWinRate;

    private ObservableList<BidHistory> allHistory = FXCollections.observableArrayList();
    private Timeline autoRefreshTimeline;

    @Override
    public void initialize() {
        // Khởi chạy đồng bộ và nạp dữ liệu lần đầu tiên
        DataStorage.autoCloseAndSaveExpiredProducts();

        // Đã xóa bớt 1 hàm loadData() thừa bạn viết trùng ở đây
        loadData();

        // Lắng nghe combobox lọc trạng thái
        if (cbFilter != null) {
            cbFilter.setOnAction(e -> applyFilter());
        }

        startAutoRefresh();
    }

    private void startAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }

        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            Thread refreshThread = new Thread(() -> {
                DataStorage.autoCloseAndSaveExpiredProducts();

                String me = DataStorage.currentAccount.getUsername();
                List<BidHistory> list = DataStorage.getBidHistory(me);

                Platform.runLater(() -> {
                    allHistory.setAll(list);
                    updateSummary();
                    applyFilter();
                });
            });
            refreshThread.setDaemon(true);
            refreshThread.start();
        }));

        autoRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            autoRefreshTimeline = null;
        }
    }

    private void loadData() {
        String me = DataStorage.currentAccount.getUsername();
        List<BidHistory> list = DataStorage.getBidHistory(me);

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // CHỈ LẤY PHIÊN ĐÃ KẾT THÚC THỰC SỰ
        List<BidHistory> finishedList = list.stream()
                .filter(h -> {
                    return h.getEndTime() != null && h.getEndTime().isBefore(now);
                })
                .collect(Collectors.toList());

        allHistory.setAll(finishedList);
        updateSummary();
        applyFilter();
    }

    @FXML
    private void onSearch(KeyEvent e) {
        applyFilter();
    }

    private void applyFilter() {
        String keyword = txtSearch != null
                ? txtSearch.getText().trim().toLowerCase() : "";
        String filter = cbFilter != null && cbFilter.getValue() != null
                ? cbFilter.getValue() : "All";

        List<BidHistory> filtered = allHistory.stream()
                .filter(h -> "WIN".equalsIgnoreCase(h.getResult()) || "LOSE".equalsIgnoreCase(h.getResult()))

                // Lọc theo từ khóa tìm kiếm tên sản phẩm
                .filter(h -> keyword.isEmpty() ||
                        h.getProductName().toLowerCase().contains(keyword))

                // Lọc theo ComboBox lựa chọn
                .filter(h -> switch (filter) {
                    case "Thắng" -> "WIN".equals(h.getResult());
                    case "Thua" -> "LOSE".equals(h.getResult());
                    default -> true;
                })
                .collect(Collectors.toList());

        renderRows(filtered);
    }

    private void renderRows(List<BidHistory> list) {
        historyContainer.getChildren().clear();

        if (list.isEmpty()) {
            Label empty = new Label("Không có lịch sử đấu giá nào đã kết thúc");
            empty.setStyle("-fx-font-size: 14; -fx-text-fill: #999;");
            empty.setPrefHeight(200);
            empty.setPrefWidth(839);
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            historyContainer.getChildren().add(empty);
            return;
        }

        for (BidHistory h : list) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(SceneConfig.BID_HISTORY_ROW.getPath())
                );
                Node row = loader.load();
                BidderHistoryRowController ctrl = loader.getController();
                ctrl.setData(h, updatedHistory -> {
                    javafx.application.Platform.runLater(() -> {
                        loadData();
                    });
                });
                historyContainer.getChildren().add(row);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateSummary() {
        // Chỉ tính toán số liệu thống kê trên các phiên thực sự đã kết thúc
        List<BidHistory> finishedRecords = allHistory.stream()
                .filter(h -> "WIN".equalsIgnoreCase(h.getResult()) || "LOSE".equalsIgnoreCase(h.getResult()))
                .collect(Collectors.toList());

        int total = finishedRecords.size();
        long win = finishedRecords.stream()
                .filter(h -> "WIN".equals(h.getResult()))
                .count();
        double rate = total > 0 ? ((double) win / total) * 100 : 0;

        if (lblTotal != null) lblTotal.setText(String.valueOf(total));
        if (lblWin != null) lblWin.setText(String.valueOf(win));
        if (lblWinRate != null) lblWinRate.setText(String.format("%.1f%%", rate));
    }


    @FXML
    @Override
    public void onMyProducts(ActionEvent event) {
        stopAutoRefresh();
        switchScene(event, SceneConfig.BIDDER_PRODUCT);
    }

    @FXML
    @Override
    public void onHistory(ActionEvent event) {
    }

    @FXML
    @Override
    public void onManage(ActionEvent event) {
        stopAutoRefresh();
        switchScene(event, SceneConfig.BIDDER_MANAGER);
    }

    @FXML
    @Override
    public void onAccount(ActionEvent event) {
        stopAutoRefresh();
        switchScene(event, SceneConfig.BIDDER_HOME);
    }

    @FXML
    @Override
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