package com.example.onlineauctionsystem.controller.common;
import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.ProductImage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.function.Consumer;

public class BidderManageRowController {

    @FXML private ImageView imgProduct;
    @FXML private Label lblProductName;
    @FXML private Label lblMyBidPrice;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblTimeRemaining;
    @FXML private Label lblStatusBadge;
    @FXML private Button btnReBid;
    private Product product;
    private Consumer<Product> onActionCallBack;

    public void setData(Product product, double myMaxBid, boolean isWinning, Consumer<Product> CallBack){
        this.product = product;
        this.onActionCallBack = CallBack;
        lblProductName.setText(product.getName());
        lblMyBidPrice.setText(String.format("%,.0fđ", myMaxBid));
        lblCurrentPrice.setText(String.format("%,.0fđ", product.getCurrentPrice()));
        lblTimeRemaining.setText(product.getRemainingTime());

        // Xử lý Badge trạng thái Đang thắng / Đang thua
        if (isWinning) {
            lblStatusBadge.setText("ĐANG THẮNG");
            lblStatusBadge.setStyle("-fx-background-color: #e8f5e9; -fx-border-color: #2e7d32; -fx-text-fill: #2e7d32; -fx-background-radius: 20; -fx-border-radius: 20; -fx-font-weight: bold;");
        } else {
            lblStatusBadge.setText("ĐANG THUA");
            lblStatusBadge.setStyle("-fx-background-color: #ffebee; -fx-border-color: #c62828; -fx-text-fill: #c62828; -fx-background-radius: 20; -fx-border-radius: 20; -fx-font-weight: bold;");
        }

        Image img = ProductImage.load(product.getImagePath(), 80, 80);
        if (img != null) {
            imgProduct.setImage(img);
        }
    }

    public void updateTime() {
        if (product != null) {
            lblTimeRemaining.setText(product.getRemainingTime());
        }
    }
    @FXML
    private void onReBidClick(ActionEvent event) {
        if (onActionCallBack != null && product != null) {
            onActionCallBack.accept(product);
        }
    }
}
