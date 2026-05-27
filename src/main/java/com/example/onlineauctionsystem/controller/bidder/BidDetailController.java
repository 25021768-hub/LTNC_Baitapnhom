package com.example.onlineauctionsystem.controller.bidder;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.ProductImage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
        product = p;
        loadStaticProductInfo();
        updateDynamicInfo();
        startTimeline();

    }

    @Override
    public void initialize() {

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
        product.updateStatus();

        lblHighestBidder.setText(Objects.requireNonNullElse(product.getHighestBidder(), "Chưa có"));
        lblCurrentPrice.setText(formatPrice(product.getCurrentPrice()));
        lblTime.setText(product.getRemainingTime());

        double balance = DataStorage.getBalance(DataStorage.currentAccount.getUsername());
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

            // 1. Cứ mỗi 5 giây: Lên Database đồng bộ thông tin đấu giá mới nhất
            if (secondsCounter >= 5) {
                secondsCounter = 0;
                Product fresh = DataStorage.findProductById(product.getId());
                if (fresh != null) {
                    this.product = fresh;
                }
            }
            updateDynamicInfo();

            // Nếu sản phẩm đã kết thúc đấu giá, ngừng Timeline luôn cho nhẹ bộ nhớ
            if ("FINISHED".equals(product.getStatus()) || "CANCELED".equals(product.getStatus())) {
                refreshTimeline.stop();
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
        if (DataStorage.currentAccount.isLocked()) {
            showAlert("Lỗi", "Tài khoản của bạn đã bị khóa! Không thể thực hiện chức năng này.");
            stopTimeline();
            // Ép đăng xuất ngay lập tức
            forceLogout(event);
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

        // Fetch giá mới nhất từ DB trước khi đặt
        Product latest = DataStorage.findProductById(product.getId());
        if (latest == null) {
            showAlert("Lỗi", "Sản phẩm không tồn tại!");
            return;
        }

        // Cập nhật lại UI với giá mới nhất
        if (latest.getCurrentPrice() != product.getCurrentPrice()) {
            product = latest;
            updateDynamicInfo();
            showAlert("Thông báo",
                    "Giá vừa được cập nhật!\n" +
                            "Giá hiện tại: " + formatPrice(product.getCurrentPrice()) + "\n" +
                            "Vui lòng nhập lại!");
            txtBidAmount.clear();
            return;
        }

        double minBid = product.getCurrentPrice() + product.getBidIncrement();
        if (bidAmount < minBid) {
            showAlert("Lỗi", "Giá đặt phải ít nhất " + formatPrice(minBid));
            return;
        }

        double balance = DataStorage.getBalance(
                DataStorage.currentAccount.getUsername()
        );
        if (bidAmount > balance) {
            showAlert("Lỗi", "Số dư không đủ!");
            return;
        }

        String bidder = DataStorage.currentAccount.getUsername();
        boolean ok = DataStorage.updateBid(product.getId(), bidAmount, bidder);

        if (ok) {
            showAlert("Thành công", "Đặt giá thành công: " + formatPrice(bidAmount));
            txtBidAmount.clear();
            product = DataStorage.findProductById(product.getId());
            if (product != null) updateDynamicInfo();
        } else {
            // Bị người khác đặt trước trong khoảnh khắc đó
            product = DataStorage.findProductById(product.getId());
            if (product != null) updateDynamicInfo();
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