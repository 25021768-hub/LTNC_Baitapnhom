package com.example.onlineauctionsystem.controller.common;

import com.example.onlineauctionsystem.model.BidHistory;
import com.example.onlineauctionsystem.model.DataStorage;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

public class BidderHistoryRowController {

    @FXML private Label lblProductName;
    @FXML private Label lblMyBidPrice;
    @FXML private Label lblFinalPrice;
    @FXML private Label lblEndTime;
    @FXML private Label lblStatusBadge;

    private BidHistory history;
    private Consumer<BidHistory> onPaidCallback;

    public void setData(BidHistory h, Consumer<BidHistory> onPaid) {
        this.history = h;
        this.onPaidCallback = onPaid;

        lblProductName.setText(h.getProductName());
        lblMyBidPrice.setText(formatPrice(h.getMyBidPrice()));
        lblFinalPrice.setText(formatPrice(h.getFinalPrice()));
        lblEndTime.setText(h.getEndTime() != null
                ? h.getEndTime().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "--/--/----");

        setStatus(h.getResult(), h.getPaid());
    }

    private void setStatus(String result, boolean isPaid) {
        if ("WIN".equals(result)) {
            if (isPaid) {
                // Đã thanh toán
                lblStatusBadge.setText("ĐÃ THANH TOÁN");
                lblStatusBadge.setTextFill(Color.web("#0099cc"));
                lblStatusBadge.setStyle(
                        "-fx-border-color: #0099cc; -fx-border-radius: 10; -fx-cursor: default;");
            } else {
                // Chờ thanh toán — bấm được
                lblStatusBadge.setText("THANH TOÁN");
                lblStatusBadge.setTextFill(Color.ORANGE);
                lblStatusBadge.setStyle(
                        "-fx-border-color: orange; -fx-border-radius: 10; " +
                                "-fx-cursor: hand; -fx-background-color: #fff8e1; " +
                                "-fx-background-radius: 10;");
                lblStatusBadge.setOnMouseClicked(e -> onPayClicked());
            }
        } else {
            // Thua
            lblStatusBadge.setOnMouseClicked(null);
            lblStatusBadge.setText("THUA");
            lblStatusBadge.setTextFill(Color.web("#ff0101"));
            lblStatusBadge.setStyle("-fx-border-color: #ff0101; -fx-border-radius: 10;");
        }
    }

    private void onPayClicked() {
        // Vô hiệu hoá ngay để chống double-click
        lblStatusBadge.setOnMouseClicked(null);
        lblStatusBadge.setStyle(lblStatusBadge.getStyle() + "; -fx-opacity: 0.5;");
        double balance = DataStorage.getBalance(
                DataStorage.currentAccount.getUsername()
        );

        if (balance < history.getFinalPrice()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Số dư không đủ");
            alert.setHeaderText("Không thể thanh toán");
            alert.setContentText(
                    "Số dư hiện tại: " + formatPrice(balance) + "\n" +
                            "Cần thanh toán: " + formatPrice(history.getFinalPrice()) + "\n" +
                            "Thiếu: " + formatPrice(history.getFinalPrice() - balance)
            );
            alert.showAndWait();
            return;
        }

        // Xác nhận thanh toán
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Thanh toán sản phẩm: " + history.getProductName());
        confirm.setContentText(
                "Số tiền cần thanh toán: " + formatPrice(history.getFinalPrice()) + "\n" +
                        "Số dư hiện tại: " + formatPrice(balance) + "\n\n" +
                        "Bạn có chắc chắn muốn thanh toán?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String currentBuyer = DataStorage.currentAccount.getUsername();

                // 1. Sử dụng hàm Transaction đồng bộ gộp (Trừ tiền buyer + Cộng tiền seller + Đổi trạng thái PAID)
                boolean transactionOk = DataStorage.executeManualPayment(currentBuyer, history.getProductId(), history.getFinalPrice());

                if (transactionOk) {

                    history.setPaid(true);
                    setStatus("WIN", true);

                    if (onPaidCallback != null) {
                        onPaidCallback.accept(history);
                    }

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Thành công");
                    success.setContentText("Thanh toán thành công!");
                    success.showAndWait();
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Lỗi");
                    error.setContentText("Thanh toán thất bại! Số dư tài khoản không đủ hoặc hệ thống bận.");
                    error.showAndWait();
                    lblStatusBadge.setStyle( "-fx-border-color: orange; -fx-border-radius: 10; " +
                            "-fx-cursor: hand; -fx-background-color: #fff8e1; " +
                            "-fx-background-radius: 10;"); // style cũ
                    lblStatusBadge.setOnMouseClicked(e -> onPayClicked());
                }
            }
            else {
                // Người dùng bấm Cancel → khôi phục
                lblStatusBadge.setOnMouseClicked(e -> onPayClicked());
                lblStatusBadge.setStyle( "-fx-border-color: orange; -fx-border-radius: 10; " +
                        "-fx-cursor: hand; -fx-background-color: #fff8e1; " +
                        "-fx-background-radius: 10;");
            }
        });
    }

    private String formatPrice(double price) {
        return String.format("%,.0fđ", price).replace(",", ".");
    }
}