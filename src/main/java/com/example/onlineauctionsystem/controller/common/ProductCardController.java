package com.example.onlineauctionsystem.controller.common;

import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.ProductImage;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

public class ProductCardController {

    @FXML private ImageView imgProduct;
    @FXML private Label lblName;
    @FXML private Label lblPrice;
    @FXML private Label lblStatus;

    private Product product;
    private Consumer<Product> onEditCallback;   // gọi lại SellerManageController khi bấm sửa
    private Consumer<Product> onDeleteCallback; // gọi lại khi bấm xóa

    public void setData(Product p,
                        Consumer<Product> onEdit,
                        Consumer<Product> onDelete) {
        this.product = p;
        this.onEditCallback = onEdit;
        this.onDeleteCallback = onDelete;

        lblName.setText(p.getName());
        lblPrice.setText("Giá khởi điểm: " + formatPrice(p.getInitialPrice()));
        lblStatus.setText("Trạng thái: " + translateStatus(p.getStatus()));

        loadImage(p.getImagePath());
    }

    @FXML
    private void onEdit() {
        if (onEditCallback != null) onEditCallback.accept(product);
    }

    @FXML
    private void onDelete() {
        // Hỏi xác nhận trước khi xóa
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa sản phẩm: " + product.getName());
        alert.setContentText("Bạn có chắc muốn xóa sản phẩm này không?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (onDeleteCallback != null) onDeleteCallback.accept(product);
        }
    }

    private void loadImage(String imagePath) {
        Image img = ProductImage.load(imagePath, 80, 87);
        if (img != null) imgProduct.setImage(img);
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "PENDING"  -> "Chờ duyệt";
            case "OPEN"     -> "Sắp diễn ra";
            case "RUNNING"  -> "Đang đấu giá";
            case "FINISHED" -> "Đã kết thúc";
            case "CANCELED" -> "Đã hủy";
            case "PAID"     -> "Đã thanh toán";
            default -> status;
        };
    }

    private String formatPrice(double price) {
        return String.format("%,.0fđ", price).replace(",", ".");
    }
}