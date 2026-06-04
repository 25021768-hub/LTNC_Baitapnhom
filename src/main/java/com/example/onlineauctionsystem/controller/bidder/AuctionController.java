package com.example.onlineauctionsystem.controller.bidder;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.model.BidHistory;
import com.example.onlineauctionsystem.model.DataStorage;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

public class AuctionController extends BaseController {
    @FXML private Pane imgContainer;
    @FXML private ImageView imgProduct;
    @FXML private Label lblProductName;
    @FXML private Label lblStep;
    @FXML private Label lblTime;

    @FXML private Label lblHighestBidder;
    @FXML private Label lblCurrentPrice;

    @FXML private Label lblBalance;
    @FXML private TextField txtBidAmount;
    @FXML private Label lblMinBid;

    @FXML private ToggleButton autoToggle;
    @FXML private Label autoStatusBadge;
    @FXML private TextField autoStepField;
    @FXML private TextField autoMaxField;
    @FXML private Label autoDot;
    @FXML private Label autoStatusText;

    @FXML private LineChart<String, Number> bidChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private Product product;
    private ScheduledExecutorService scheduler;
    private Timeline refreshTimeline;
    private int secondsCounter = 0;

    public void setProduct(Product p) {
        this.product = p;
        loadStaticProductInfo();
        updateDynamicInfo();
        startTimeline();
    }

    @Override
    public void initialize() {
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

        imgContainer.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            imgProduct.setFitWidth(newVal.getWidth());
            imgProduct.setFitHeight(newVal.getHeight());
        });

        setUpCharConfig();
    }

    private void loadStaticProductInfo() {
        lblProductName.setText(product.getName());
        lblStep.setText(formatPrice(product.getBidIncrement()));
        autoStepField.setText(String.format("%.0f", product.getBidIncrement()));

        Image img = ProductImage.load(product.getImagePath(), 229, 150);
        if (img != null) imgProduct.setImage(img);
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

        double balance = DataStorage.getBalance(DataStorage.currentAccount.getUsername());
        lblBalance.setText(formatPrice(balance));

        double minBid = product.getCurrentPrice() + product.getBidIncrement();
        lblMinBid.setText(formatPrice(minBid));
    }

    private void refreshChartData() {
        if (product == null) return;
        Platform.runLater(() -> {
            XYChart.Series<String, Number> series = DataStorage.getProductChartData(product.getId(), product.getName());
            bidChart.getData().clear();
            bidChart.getData().add(series);
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

                Thread dbThread = new Thread(() -> {
                    Product fresh = DataStorage.findProductById(product.getId());
                    if (fresh != null) {
                        Platform.runLater(() -> {
                            this.product = fresh;
                            this.product.updateStatus();
                            updateDynamicInfo();
                            refreshChartData();
                        });
                    }
                });
                dbThread.setDaemon(true);
                dbThread.start();
            } else {
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
    }

    private void setUpCharConfig() {
        xAxis.setLabel("Thời gian đấu giá");
        yAxis.setLabel("Giá tiền(VND)");
        bidChart.setAnimated(false);
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);
    }

    @FXML
    private void onPlaceBid(ActionEvent event) {
        if (DataStorage.currentAccount.isLocked()) {
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

        Product latest = DataStorage.findProductById(product.getId());
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

        double balance = DataStorage.getBalance(DataStorage.currentAccount.getUsername());
        if (bidAmount > balance) {
            showAlert("Lỗi", "Số dư không đủ!");
            return;
        }

        String bidder = DataStorage.currentAccount.getUsername();
        boolean ok = DataStorage.updateBid(product.getId(), bidAmount, bidder);

        if (ok) {
            BidHistory bidHistory = new BidHistory(product.getId(), product.getName(), bidder, bidAmount, bidAmount, product.getEndTime(), "WIN", false);
            DataStorage.saveBidHistory(bidHistory);
            showAlert("Thành công", "Đặt giá thành công: " + formatPrice(bidAmount));
            txtBidAmount.clear();
            Product freshSuccess = DataStorage.findProductById(product.getId());
            if (freshSuccess != null) {
                this.product = freshSuccess;
                updateDynamicInfo();
                refreshChartData();
            }
        } else {
            Product freshFail = DataStorage.findProductById(product.getId());
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
    private void handleAutoToggle() {
        if (product == null) {
            autoToggle.setSelected(false);
            return;
        }

        if (autoToggle.isSelected()) {
            String maxRaw = autoMaxField.getText().trim();
            if (maxRaw.isEmpty()) {
                showAlert("Thông báo", "Vui lòng điền mức giới hạn tối đa trước khi kích hoạt Auto Bid!");
                autoToggle.setSelected(false);
                return;
            }
            try {
                double maxPriceLimit = Double.parseDouble(maxRaw);
                double minBidRequired = product.getCurrentPrice() + product.getBidIncrement();

                if (maxPriceLimit < minBidRequired) {
                    showAlert("Lỗi cấu hình", "Giới hạn tối đa không được thấp hơn mức thầu hợp lệ tiếp theo: " + formatPrice(minBidRequired));
                    autoToggle.setSelected(false);
                    return;
                }
                String username = DataStorage.currentAccount.getUsername();
                boolean isSaved = DataStorage.setupAutoBid(username, product.getId(), maxPriceLimit);

                if (isSaved) {
                    autoToggle.setText("Bật");
                    autoToggle.setStyle("-fx-background-color: #3cc41e; -fx-background-radius: 12; -fx-text-fill: white;");

                    autoStatusBadge.setVisible(true);
                    autoStatusBadge.setText("ACTIVE");
                    autoDot.setStyle("-fx-text-fill: #3cc41e; -fx-font-size: 10;");
                    autoStatusText.setText("Auto bid đang chạy. Tối đa: " + formatPrice(maxPriceLimit));

                    autoMaxField.setDisable(true);

                    DataStorage.triggerAutoBidSystem(product.getId(), product.getCurrentPrice(), product.getBidIncrement());

                    Product fresh = DataStorage.findProductById(product.getId());
                    if (fresh != null) {
                        this.product = fresh;
                        updateDynamicInfo();
                        refreshChartData();
                    }
                } else {
                    autoToggle.setSelected(false);
                    showAlert("Lỗi", "Không thể ghi nhận thiết lập cấu hình tự động đấu giá.");
                }
            } catch (NumberFormatException e) {
                autoToggle.setSelected(false);
                showAlert("Lỗi", "Số tiền giới hạn nhập vào không hợp lệ!");
            }
        } else {
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
