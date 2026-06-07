package com.example.onlineauctionsystem.controller.bidder;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.model.Account;
import com.example.onlineauctionsystem.model.BidHistory;
import com.example.onlineauctionsystem.model.RemoteDataStorage;
import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.ProductImage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

public class AuctionController extends BaseController {
    // ── Thẻ SP bên trái ───────────────────────────────────────────
    @FXML
    private ImageView imgProduct;
    @FXML private Label lblProductName;
    @FXML private Label lblStep;
    @FXML private Label lblTime;

    // ── Thông tin hiện tại ────────────────────────────────────────
    @FXML private Label lblHighestBidder;
    @FXML private Label lblCurrentPrice;

    // ── Đặt giá ───────────────────────────────────────────────────
    @FXML private Label lblBalance;
    @FXML private TextField txtBidAmount;
    @FXML private Label lblMinBid;

    // ──Điều khiển cấu hình Auto Bid theo FXML ──────────
    @FXML private ToggleButton autoToggle;
    @FXML private Label autoStatusBadge;
    @FXML private TextField autoStepField;
    @FXML private TextField autoMaxField;
    @FXML private Label autoDot;
    @FXML private Label autoStatusText;

    // ── Khai báo thành phần biểu đồ LineChart theo FXML ──
    @FXML private LineChart<String, Number> bidChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private Product product;
    private ScheduledExecutorService scheduler;
    private Timeline refreshTimeline;
    private int secondsCounter = 0;

    // FIX : Thay thế new Thread() mỗi lần bằng một ExecutorService dùng chung.
    // Trước đây: mỗi giây updateDynamicInfo() + refreshChartData() mỗi hàm tạo 1 Thread mới.
    // Nếu server phản hồi chậm, hàng chục thread chồng chất → memory leak + race condition.
    // Giải pháp: dùng single-thread executor để đảm bảo các tác vụ nối tiếp nhau,
    // không tạo thread thừa, và có thể shutdown() sạch khi rời màn hình.
    private final java.util.concurrent.ExecutorService bgExecutor =
            java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "auction-bg-worker");
                t.setDaemon(true);
                return t;
            });

    public void setProduct(Product p) {
        this.product = p;
        loadStaticProductInfo();
        updateDynamicInfo();
        startTimeline();
    }

    @Override
    public void initialize() {
        //Chỉ cho nhập số
        txtBidAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtBidAmount.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        autoMaxField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                autoMaxField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        setUpCharConfig();
    }

    private void loadStaticProductInfo() {
        lblProductName.setText(product.getName());;
        lblStep.setText(formatPrice(product.getBidIncrement()));
        autoStepField.setText(String.format("%.0f", product.getBidIncrement()));

        ProductImage.loadAsync(product.getImagePath(), 229, 150, imgProduct);
    }

    private void updateDynamicInfo() {
        if (product == null) return;
        if (product.getEndTime() == null && product.getStartTime() != null) {
            product.setEndTime(product.getStartTime().plusHours(product.getDurationHours()));
        }
        product.updateStatus();

        lblHighestBidder.setText(Objects.requireNonNullElse(product.getHighestBidder(), "Chưa có"));
        lblCurrentPrice.setText(formatPrice(product.getCurrentPrice()));
        lblTime.setText(product.getRemainingTime());

        double minBid = product.getCurrentPrice() + product.getBidIncrement();
        lblMinBid.setText(formatPrice(minBid));

        // FIX : Dùng bgExecutor thay vì new Thread() mỗi giây
        String username = RemoteDataStorage.currentAccount.getUsername();
        bgExecutor.submit(() -> {
            double balance = RemoteDataStorage.getBalance(username);
            javafx.application.Platform.runLater(() -> lblBalance.setText(formatPrice(balance)));
        });
    }
    private void refreshChartData() {
        if (product == null) return;
        // FIX : Dùng bgExecutor thay vì new Thread() — tránh chồng thread khi server chậm
        String productId   = product.getId();
        String productName = product.getName();
        bgExecutor.submit(() -> {
            XYChart.Series<String, Number> series =
                    RemoteDataStorage.getProductChartData(productId, productName);
            Platform.runLater(() -> {
                bidChart.getData().clear();
                bidChart.getData().add(series);
            });
        });
    }

    private void startTimeline() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }

        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsCounter++;

            if (secondsCounter >= 5) {
                secondsCounter = 0;

                // FIX : Dùng bgExecutor thay vì new Thread() — tránh tích lũy thread khi server chậm
                bgExecutor.submit(() -> {
                    Product fresh = RemoteDataStorage.findProductById(product.getId());
                    if (fresh != null) {
                        Platform.runLater(() -> {
                            this.product = fresh;
                            this.product.updateStatus();
                            updateDynamicInfo();
                            refreshChartData();
                        });
                    }
                });
            } else {
                // Các giây lẻ chỉ làm nhiệm vụ trừ thời gian đếm ngược cục bộ
                updateDynamicInfo();
            }

            if (product != null && ("FINISHED".equalsIgnoreCase(product.getStatus()) || "CANCELED".equalsIgnoreCase(product.getStatus()))) {
                lblTime.setText("Đã kết thúc");
                stopTimeline();
            }
        }));

        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    private void stopTimeline() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            refreshTimeline = null;
        }
        // FIX : Shutdown executor khi rời màn hình để giải phóng thread
        if (!bgExecutor.isShutdown()) {
            bgExecutor.shutdownNow();
        }
    }

    private void setUpCharConfig(){
        xAxis.setLabel("Thời gian đấu giá");
        yAxis.setLabel("Giá tiền(VND)");
        // Tắt hiệu ứng mặc định của JavaFX khi cập nhật để đường đồ thị nhảy Realtime không bị giật, lag
        bidChart.setAnimated(false);
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false); // Không ép trục Y phải bắt đầu từ số 0
    }

    @FXML
    private void onPlaceBid(ActionEvent event) {
        Account freshAccount = RemoteDataStorage.findAccountByUsername(
                RemoteDataStorage.currentAccount.getUsername());
        if (freshAccount == null || freshAccount.isLocked()) {
            showAlert("Lỗi", "Tài khoản của bạn đã bị khóa! Không thể thực hiện chức năng này.");
            stopTimeline();
            forceLogout(event);
            return;
        }

        if (product == null || "FINISHED".equalsIgnoreCase(product.getStatus())) {
            showAlert("Thông báo", "Phiên đấu giá này đã kết thúc! Bạn không thể đặt giá thêm.");
            stopTimeline();
            onBack(event);
            return;
        }

        String input = txtBidAmount.getText().trim().replace(".", "");
        if (input.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập số tiền!");
            return;
        }

        double bidAmount;
        try {
            bidAmount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số tiền không hợp lệ!");
            return;
        }

        // Fetch giá mới nhất từ DB ngay trước khi nhấn nút ghi nhận
        Product latest = RemoteDataStorage.findProductById(product.getId());
        if (latest == null) {
            showAlert("Lỗi", "Sản phẩm không tồn tại!");
            return;
        }

        latest.updateStatus();
        if ("FINISHED".equalsIgnoreCase(latest.getStatus())) {
            this.product = latest;
            stopTimeline();
            showAlert("Thất bại", "Quá muộn! Phiên đấu giá vừa mới kết thúc cách đây vài giây.");
            txtBidAmount.clear();
            onBack(event);
            return;
        }

        // Cập nhật lại UI nếu phát hiện có người thay đổi giá trước đó một bước
        double priceDifference = Math.abs(latest.getCurrentPrice() - product.getCurrentPrice());

        if (priceDifference > 0.001) {
            this.product = latest;
            updateDynamicInfo();
            showAlert("Thông báo",
                    "Giá vừa được người khác cập nhật!\n" +
                            "Giá hiện tại: " + formatPrice(product.getCurrentPrice()) + "\n" +
                            "Vui lòng nhập lại mức giá mới!");
            txtBidAmount.clear();
            return;
        }

        double minBid = product.getCurrentPrice() + product.getBidIncrement();
        if (bidAmount < minBid) {
            showAlert("Lỗi", "Giá đặt phải ít nhất " + formatPrice(minBid));
            return;
        }

        double balance = RemoteDataStorage.getBalance(RemoteDataStorage.currentAccount.getUsername());
        if (bidAmount > balance) {
            showAlert("Lỗi", "Số dư không đủ!");
            return;
        }

        String bidder = RemoteDataStorage.currentAccount.getUsername();
        boolean ok = RemoteDataStorage.updateBid(product.getId(), bidAmount, bidder);

        if (ok) {
            showAlert("Thành công", "Đặt giá thành công: " + formatPrice(bidAmount));
            txtBidAmount.clear();
            Product freshSuccess = RemoteDataStorage.findProductById(product.getId());
            if (freshSuccess != null) {
                this.product = freshSuccess;
                updateDynamicInfo();
                refreshChartData();
            }
        } else {
            // Bị người khác chèn lệnh đặt trước trong khoảnh khắc tích tắc đó
            Product freshFail = RemoteDataStorage.findProductById(product.getId());
            if (freshFail != null) {
                this.product = freshFail;
                updateDynamicInfo();
                refreshChartData();
            }
            showAlert("Thất bại",
                    "Có người vừa đặt giá trước bạn!\n" +
                            "Giá hiện tại: " + formatPrice(product.getCurrentPrice()) + "\n" +
                            "Vui lòng nhập lại!");
            txtBidAmount.clear();
        }
    }

    @FXML
    private void handleAutoToggle(){
        if (product == null) {
            autoToggle.setSelected(false);
            return;
        }

        if(autoToggle.isSelected()){
            String maxRaw = autoMaxField.getText().trim();
            if(maxRaw.isEmpty()){
                showAlert("Thông báo", "Vui lòng điền mức giới hạn tối đa trước khi kích hoạt Auto Bid!");
                autoToggle.setSelected(false);
                return;
            }
            try{
                double maxPriceLimit = Double.parseDouble(maxRaw);
                double minBidRequired = product.getCurrentPrice() + product.getBidIncrement();

                if (maxPriceLimit < minBidRequired) {
                    showAlert("Lỗi cấu hình", "Giới hạn tối đa không được thấp hơn mức thầu hợp lệ tiếp theo: " + formatPrice(minBidRequired));
                    autoToggle.setSelected(false);
                    return;
                }
                String username = RemoteDataStorage.currentAccount.getUsername();
                boolean isSaved = RemoteDataStorage.setupAutoBid(username, product.getId(), maxPriceLimit);

                if (isSaved) {
                    autoStepField.setDisable(true);
                    autoMaxField.setDisable(true);
                    // Đồng bộ giao diện sang chế độ KÍCH HOẠT (ACTIVE) theo FXML style
                    autoToggle.setText("Bật");
                    autoToggle.setStyle("-fx-background-color: #3cc41e; -fx-background-radius: 12; -fx-text-fill: white;");

                    autoStatusBadge.setVisible(true);
                    autoStatusBadge.setText("ACTIVE");
                    autoDot.setStyle("-fx-text-fill: #3cc41e; -fx-font-size: 10;");
                    autoStatusText.setText("Auto bid đang chạy. Tối đa: " + formatPrice(maxPriceLimit));

                    autoMaxField.setDisable(true);

                    // Kích hoạt ngay chuỗi đệ quy xử lý nâng giá tự động trong DB
                    RemoteDataStorage.triggerAutoBidSystem(product.getId(), product.getCurrentPrice(), product.getBidIncrement());

                    // Đồng bộ làm mới lập tức thông tin hiển thị và biểu đồ sau luồng AutoBid
                    Product fresh = RemoteDataStorage.findProductById(product.getId());
                    if (fresh != null) {
                        this.product = fresh;
                        updateDynamicInfo();
                        refreshChartData();
                    }
                } else {
                    autoToggle.setSelected(false);
                    showAlert("Lỗi", "Không thể ghi nhận thiết lập cấu hình tự động đấu giá.");
                }
            }
            catch (NumberFormatException e) {
                autoToggle.setSelected(false);
                showAlert("Lỗi", "Số tiền giới hạn nhập vào không hợp lệ!");
            }
        }
        else {
            // Trường hợp: Người dùng nhấn tắt hệ thống AutoBid
            autoToggle.setText("Tắt");
            autoToggle.setStyle("-fx-background-color: #888780; -fx-background-radius: 12; -fx-text-fill: white;");

            autoStatusBadge.setVisible(false);
            autoStatusBadge.setText("");
            autoDot.setStyle("-fx-text-fill: #888780; -fx-font-size: 10;");
            autoStatusText.setText("Auto bid chưa kích hoạt");

            autoMaxField.setDisable(false);
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        stopTimeline();
        switchScene(event, SceneConfig.BIDDER_PRODUCT);
    }
}