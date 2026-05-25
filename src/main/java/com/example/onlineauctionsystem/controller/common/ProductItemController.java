package com.example.onlineauctionsystem.controller.seller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.example.onlineauctionsystem.model.Product;

public class ProductRowController {

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
        lblStartPrice.setText(formatPrice(p.getStartPrice()));
        lblStep.setText(formatPrice(p.getStep()));
        lblTime.setText(p.getRemainingTime());

        // Load ảnh bất đồng bộ (không block UI)
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            Image img = new Image(p.getImageUrl(), 90, 90, true, true, true);
            imgProduct.setImage(img);
        }
    }

    private String formatPrice(long price) {
        return String.format("%,dđ", price).replace(",", ".");
    }
}