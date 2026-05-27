package com.example.onlineauctionsystem.controller.common;

import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.ProductImage;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.function.Consumer;

public class BidNewProductCardController {

    @FXML private ImageView imgProduct;
    @FXML private Label lblName;
    @FXML private Label lblPrice;
    @FXML private Label lblTime;
    private Consumer<Product> onBidCallBack;
    private Product product;
    public void setData(Product p, Consumer<Product> CallBack){
        this.product = p;
        this.onBidCallBack = CallBack;
        lblName.setText(p.getName());
        lblPrice.setText(formatPrice(p.getCurrentPrice()));
        lblTime.setText(p.getRemainingTime());
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            Image img = ProductImage.load(p.getImagePath(), 110, 110);
            if (img != null) imgProduct.setImage(img);
        }
    }
    public void updateTime() {
        if (product != null) {
            lblTime.setText(product.getRemainingTime());
            lblPrice.setText(formatPrice(product.getCurrentPrice()));

        }
    }
    @FXML
    private void onBid() {
    if(onBidCallBack != null) onBidCallBack.accept(product);
    }
    private String formatPrice(double price) {
        return String.format("%,.0fđ", price).replace(",", ".");
    }
}
