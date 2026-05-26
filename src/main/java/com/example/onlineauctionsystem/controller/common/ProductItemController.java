package com.example.onlineauctionsystem.controller.common;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.example.onlineauctionsystem.model.Product;

public class ProductItemController {

    @FXML private ImageView imgProduct;
    @FXML private Label lblName;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblBidCount;
    @FXML private Label lblStartPrice;
    @FXML private Label lblStep;
    @FXML private Label lblTime;

    public void setData(Product p) {
        lblName.setText(p.getName());
        lblCurrentPrice.setText(formatPrice(p.getCurrentPrice()));
        lblStartPrice.setText(formatPrice(p.getInitialPrice()));
        lblStep.setText(formatPrice(p.getBidIncrement()));
        lblTime.setText(String.valueOf(p.getDurationHours()));

        // Load ảnh bất đồng bộ (không block UI)
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            Image img = new Image(p.getImagePath(), 90, 90, true, true, true);
            imgProduct.setImage(img);
        }
    }

    private String formatPrice(double price) {
        return String.format("%,dđ", price).replace(",", ".");
    }
}