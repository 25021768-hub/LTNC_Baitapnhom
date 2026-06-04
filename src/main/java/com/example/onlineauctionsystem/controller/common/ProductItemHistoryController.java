package com.example.onlineauctionsystem.controller.common;

import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.ProductImage;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.format.DateTimeFormatter;

public class ProductItemHistoryController {
    @FXML private ImageView imgProduct;
    @FXML private Label lblName;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblcurrentBidder;
    @FXML private Label lblsellDate;
    @FXML private Label lblStatus;
    public void setData(Product p) {
        lblName.setText(p.getName());
        lblCurrentPrice.setText(formatPrice(p.getCurrentPrice()));
        lblcurrentBidder.setText(p.getHighestBidder());

        if (p.getStartTime() != null) {
            lblsellDate.setText(
                    p.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
        } else {
            lblsellDate.setText("--/--/----");
        }
        switch (p.getStatus()) {
            case "FINISHED" -> {
                lblStatus.setText("Đợi thanh toán");
                lblStatus.setStyle("-fx-background-color: yellow; -fx-text-fill: black; -fx-background-radius: 20; -fx-border-radius: 20;");
            }
            case "PAID" -> {
                lblStatus.setText("Thành công");
                lblStatus.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-background-radius: 20; -fx-border-radius: 20;");
            }
            case "CANCELED" -> {
                lblStatus.setText("Đã hủy");
                lblStatus.setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-background-radius: 20; -fx-border-radius: 20;");
            }
        }
        // Load ảnh bất đồng bộ (không block UI)
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            Image img = ProductImage.load(p.getImagePath(), 110, 110);
            if (img != null) imgProduct.setImage(img);
        }
    }

    public String formatPrice(double price) {
        return String.format("%,.0fđ", price).replace(",", ".");
    }

}
