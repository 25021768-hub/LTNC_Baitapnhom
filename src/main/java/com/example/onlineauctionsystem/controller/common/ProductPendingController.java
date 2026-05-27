package com.example.onlineauctionsystem.controller.common;

import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.ProductImage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.function.BiConsumer;

public class ProductPendingController {
    @FXML private ImageView imgProduct;
    @FXML private Label lblProductName;
    @FXML private Label lblSellerName;
    @FXML private Label lblStartPrice;
    @FXML private Label lblPriceStep;
    @FXML private Label lblDuration;
    private Product product;
    private BiConsumer<Product, Boolean> onActionCallBack;
    public void setData(Product p, BiConsumer<Product,Boolean> callBack){
        this.product = p;
        this.onActionCallBack = callBack;
        lblProductName.setText(p.getName());
        lblDuration.setText(String.valueOf(p.getDurationHours() + "h"));
        lblSellerName.setText(p.getSellerName());
        lblStartPrice.setText(formatPrice(p.getInitialPrice()));
        lblPriceStep.setText(formatPrice(p.getBidIncrement()));
        loadImage(p.getImagePath());
    }
    private void loadImage(String imagePath) {
        Image img = ProductImage.load(imagePath, 80, 87);
        if (img != null) imgProduct.setImage(img);
    }
    private String formatPrice(double price) {
        return String.format("%,.0fđ", price).replace(",", ".");
    }
    @FXML
    private void onApproveAction(ActionEvent event) {
        if (onActionCallBack != null) onActionCallBack.accept(product, true);
    }
    @FXML
    private void onRejectAction(ActionEvent event) {
        if (onActionCallBack != null) onActionCallBack.accept(product, false);
    }
}
