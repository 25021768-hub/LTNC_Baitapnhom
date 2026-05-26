package com.example.onlineauctionsystem.controller.common;

import com.example.onlineauctionsystem.utils.ProductImage;
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
        lblBidCount.setText(String.valueOf(p.getBidIncrement()));

        // Load ảnh bất đồng bộ (không block UI)
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            Image img = ProductImage.load(p.getImagePath(), 110, 110);
            if (img != null) imgProduct.setImage(img);
        }
    }

    private String formatPrice(double price) {
        return String.format("%,dđ", price).replace(",", ".");
    }
}