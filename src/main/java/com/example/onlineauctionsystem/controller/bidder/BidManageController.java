package com.example.onlineauctionsystem.controller.bidder;

import com.example.onlineauctionsystem.controller.MenuController;
import com.example.onlineauctionsystem.controller.common.BidderManageRowController;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BidManageController extends MenuController {

    @FXML private VBox rowsContainer;
    @FXML private Label lblTotalBidding;
    @FXML private Label lblTotalWinning;
    @FXML private Label lblTotalValue;
    @FXML private Label lblHotWinning;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> comboStatus;

    private final ObservableList<Product> runningBidList = FXCollections.observableArrayList();
    private final List<BidderManageRowController> rowControllers = new ArrayList<>();

    private ScheduledExecutorService scheduler;
    private int refreshCounter = 0;
    private final int DATA_REFRESH_INTERVAL = 5;

    public void initialize() {
        // Mỗi khi danh sách thay đổi thì áp dụng bộ lọc và vẽ lại giao diện
        runningBidList.addListener((ListChangeListener<Product>) change -> applyFilterAndRender());
        List<Product> initialData = fetchRunningBids();
        runningBidList.setAll(initialData);
        startAutoRefresh();

        // Lắng nghe khi node được gắn vào hoặc tháo khỏi scene.
        // Khi scene == null, tức màn hình đã chuyển đi, dừng scheduler để tránh rò thread.
        rowsContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                stopAutoRefresh();
            } else {
                // Khi có cửa sổ, đăng ký thêm: đóng cửa sổ (nhấn X) cũng dừng scheduler
                newScene.windowProperty().addListener((wObs, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.setOnHiding(we -> stopAutoRefresh());
                    }
                });
            }
        });
    }

    private List<Product> fetchRunningBids() {
        if (RemoteDataStorage.currentAccount == null) return new ArrayList<>();
        return RemoteDataStorage.getRunningBidsByBidder(RemoteDataStorage.currentAccount.getUsername());
    }

    private void startAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) return;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "bid-manage-refresh");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            refreshCounter++;
            if (refreshCounter >= DATA_REFRESH_INTERVAL) {
                // Mỗi 5 giây lấy dữ liệu mới từ server
                refreshCounter = 0;
                List<Product> freshData = fetchRunningBids();
                Platform.runLater(() -> runningBidList.setAll(freshData));
            } else {
                // Các giây còn lại chỉ cập nhật đồng hồ đếm ngược trên giao diện
                Platform.runLater(() -> {
                    for (BidderManageRowController ctrl : rowControllers) {
                        ctrl.updateTime();
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void stopAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void applyFilterAndRender() {
        String keyword = (txtSearch != null) ? txtSearch.getText().trim().toLowerCase() : "";
        String statusFilter = (comboStatus != null && comboStatus.getValue() != null) ? comboStatus.getValue() : "All";
        String currentUsername = RemoteDataStorage.currentAccount.getUsername();

        // Lọc danh sách theo từ khóa tìm kiếm và trạng thái thắng/thua
        List<Product> filteredList = runningBidList.stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(keyword))
                .filter(p -> {
                    if ("All".equals(statusFilter)) return true;
                    boolean isWinning = currentUsername.equalsIgnoreCase(p.getHighestBidder());
                    if ("Đang thắng".equals(statusFilter)) return isWinning;
                    if ("Đang thua".equals(statusFilter)) return !isWinning;
                    return true;
                })
                .collect(Collectors.toList());

        // Xóa các hàng cũ rồi vẽ lại theo danh sách đã lọc
        rowsContainer.getChildren().clear();
        rowControllers.clear();

        if (filteredList.isEmpty()) {
            Label emptyLabel = new Label("Không tìm thấy phiên đấu giá nào phù hợp");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #888888; -fx-padding: 20;");
            rowsContainer.getChildren().add(emptyLabel);
        } else {
            for (Product p : filteredList) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneConfig.BID_MANAGE_ROW.getPath()));
                    Node rowNode = loader.load();

                    BidderManageRowController rowCtrl = loader.getController();

                    boolean isWinning = currentUsername.equalsIgnoreCase(p.getHighestBidder());
                    double myMaxBid = RemoteDataStorage.getMaxBidByBidderForProduct(currentUsername, p.getId(), p.getCurrentPrice());

                    // Truyền dữ liệu vào hàng con, kèm callback điều hướng khi nhấn nút tái đấu
                    rowCtrl.setData(p, myMaxBid, isWinning, this::navigateToBidDetail);

                    rowsContainer.getChildren().add(rowNode);
                    rowControllers.add(rowCtrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        calculateSummary();
    }

    private void calculateSummary() {
        String currentUsername = RemoteDataStorage.currentAccount.getUsername();
        int totalBidding = runningBidList.size();
        // Snapshot tránh ConcurrentModificationException khi list thay đổi trong lúc đếm
        List<Product> snapshot = new ArrayList<>(runningBidList);

        Thread t = new Thread(() -> {
            long totalWinning = snapshot.stream()
                    .filter(p -> currentUsername.equalsIgnoreCase(p.getHighestBidder()))
                    .count();

            // Network call lấy giá thầu tối đa — phải chạy trên background thread
            double totalValue = snapshot.stream()
                    .mapToDouble(p -> RemoteDataStorage.getMaxBidByBidderForProduct(
                            currentUsername, p.getId(), p.getCurrentPrice()))
                    .sum();

            long hotWinning = snapshot.stream()
                    .filter(p -> currentUsername.equalsIgnoreCase(p.getHighestBidder()))
                    .filter(p -> p.getRemainingTime() != null &&
                            (p.getRemainingTime().contains("00:01:") || p.getRemainingTime().contains("00:00:")))
                    .count();

            final long tw = totalWinning;
            final double tv = totalValue;
            final long hw = hotWinning;

            // Cập nhật label phải thực hiện trên JavaFX Application Thread
            Platform.runLater(() -> {
                lblTotalBidding.setText("Đang Đấu: " + totalBidding);
                lblTotalWinning.setText("Bạn đang dẫn đầu: " + tw);
                lblTotalValue.setText("Tổng giá trị thầu: " + String.format("%,.0fđ", tv));
                lblHotWinning.setText("Bạn đang dẫn đầu (2 phút cuối): " + hw);
            });
        }, "calculate-summary-thread");
        t.setDaemon(true);
        t.start();
    }

    private void navigateToBidDetail(Product p) {
        stopAutoRefresh();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneConfig.BIDDER_WINDOW.getPath()));
            Parent root = loader.load();

            AuctionController ctrl = loader.getController();
            ctrl.setProduct(p);

            Stage stage = (Stage) rowsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(SceneConfig.BIDDER_WINDOW.getTitle());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onSearch(KeyEvent keyEvent) {
        applyFilterAndRender();
    }

    @FXML
    public void onFilterStatus(ActionEvent event) {
        applyFilterAndRender();
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
