package com.example.onlineauctionsystem.controller.common;

import com.example.onlineauctionsystem.utils.ProductImage;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.scene.image.ImageView;
import com.example.onlineauctionsystem.model.Product;

public class ProductItemController {

    @FXML private ImageView imgProduct;
    @FXML private Label lblName;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblStartPrice;
    @FXML private Label lblStep;
    @FXML private Label lblTime;
    private Product product;
    public void setData(Product p) {
        this.product = p;
        lblName.setText(p.getName());
        lblCurrentPrice.setText(formatPrice(p.getCurrentPrice()));
        lblStartPrice.setText(formatPrice(p.getInitialPrice()));
        lblStep.setText(formatPrice(p.getBidIncrement()));
        lblTime.setText(String.valueOf(p.getRemainingTime()));

        // Load ảnh bất đồng bộ (không block UI)
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            ProductImage.loadAsync(p.getImagePath(), 110, 110, imgProduct);
        }
    }


    public void updateTime() {
        if (product != null) {
            lblTime.setText(product.getRemainingTime());
            lblCurrentPrice.setText(formatPrice(product.getCurrentPrice()));
        }
    }
    private String formatPrice(double price) {
        return String.format("%,.0fđ", price).replace(",", ".");
    }
}