package com.example.onlineauctionsystem.controller.bidder;
import com.example.onlineauctionsystem.controller.BaseController;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

public class BidDetailController extends BaseController {
    // ── Thẻ SP bên trái ───────────────────────────────────────────
    @FXML private ImageView imgProduct;
    @FXML private Label lblProductName;
    @FXML private Label lblStep;
    @FXML private Label lblSeller;
    @FXML private Label lblInitialPrice;
    @FXML private Label lblTime;

    // ── Thông tin hiện tại ────────────────────────────────────────
    @FXML private Label lblCurrentName;
    @FXML private Label lblHighestBidder;
    @FXML private Label lblCurrentPrice;

    // ── Đặt giá ───────────────────────────────────────────────────
    @FXML private Label lblBalance;
    @FXML private TextField txtBidAmount;
    @FXML private Label lblMinBid;

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
        //Chỉ cho nhập số
        txtBidAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtBidAmount.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void loadStaticProductInfo() {
        lblProductName.setText(product.getName());
        lblCurrentName.setText(product.getName());
        lblStep.setText(formatPrice(product.getBidIncrement()));
        lblSeller.setText(product.getSellerName());
        lblInitialPrice.setText(formatPrice(product.getInitialPrice()));

        Image img = ProductImage.load(product.getImagePath(), 229, 150);
        if (img != null) imgProduct.setImage(img);
    }

    private void updateDynamicInfo() {
        if (product == null) return;
        if (product.getEndTime() == null && product.getStartTime() != null) {
            product.setEndTime(product.getStartTime().plusHours(product.getDurationHours()));
        }
        // Luôn luôn tính toán lại trạng thái dựa trên thời gian thực
        product.updateStatus();

        lblHighestBidder.setText(Objects.requireNonNullElse(product.getHighestBidder(), "Chưa có"));
        lblCurrentPrice.setText(formatPrice(product.getCurrentPrice()));
        lblTime.setText(product.getRemainingTime());

        double balance = RemoteDataStorage.getBalance(RemoteDataStorage.currentAccount.getUsername());
        lblBalance.setText(formatPrice(balance));

        double minBid = product.getCurrentPrice() + product.getBidIncrement();
        lblMinBid.setText(formatPrice(minBid));
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
                    Product fresh = RemoteDataStorage.findProductById(product.getId());
                    if (fresh != null) {
                        Platform.runLater(() -> {
                            this.product = fresh;
                            this.product.updateStatus(); // Đồng bộ trạng thái mới
                            updateDynamicInfo();
                        });
                    }
                });
                dbThread.setDaemon(true);
                dbThread.start();
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
    }

    @FXML
    private void onPlaceBid(ActionEvent event) {
        if (RemoteDataStorage.currentAccount.isLocked()) {
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
            BidHistory bidHistory = new BidHistory(product.getId(), product.getName(), bidder, bidAmount, bidAmount , product.getEndTime(),"WIN", false);
            RemoteDataStorage.saveBidHistory(bidHistory);
            showAlert("Thành công", "Đặt giá thành công: " + formatPrice(bidAmount));
            txtBidAmount.clear();
            Product freshSuccess = RemoteDataStorage.findProductById(product.getId());
            if (freshSuccess != null) {
                this.product = freshSuccess;
                updateDynamicInfo();
            }
        } else {
            // Bị người khác chèn lệnh đặt trước trong khoảnh khắc tích tắc đó
            Product freshFail = RemoteDataStorage.findProductById(product.getId());
            if (freshFail != null) {
                this.product = freshFail;
                updateDynamicInfo();
            }
            showAlert("Thất bại",
                    "Có người vừa đặt giá trước bạn!\n" +
                            "Giá hiện tại: " + formatPrice(product.getCurrentPrice()) + "\n" +
                            "Vui lòng nhập lại!");
            txtBidAmount.clear();
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        stopTimeline();
        switchScene(event, SceneConfig.BIDDER_PRODUCT);
    }
}