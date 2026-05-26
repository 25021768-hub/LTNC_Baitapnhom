package com.example.onlineauctionsystem.controller.common;

import com.example.onlineauctionsystem.model.Account;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.util.function.Consumer;

public class UserController {

    @FXML private ImageView imgAvatar;
    @FXML private Label lblFullName;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblIdCard;
    @FXML private Label lblRole;
    @FXML private Button btnToggle;

    private Account account;
    private Consumer<Account> onToggleCallback;

    public void setData(Account acc, Consumer<Account> onToggle) {
        this.account = acc;
        this.onToggleCallback = onToggle;

        lblFullName.setText(acc.getFullName());
        lblEmail.setText(acc.getEmail());
        lblPhone.setText(acc.getPhoneNumber());
        lblIdCard.setText(acc.getIdCard());
        lblRole.setText(translateRole(acc.getRole()));

        // Cập nhật nút theo trạng thái
        updateToggleButton(acc.isLocked());
    }

    @FXML
    private void onToggleLock() {
        if (onToggleCallback != null) onToggleCallback.accept(account);
    }

    // Gọi từ bên ngoài để cập nhật lại nút sau khi toggle
    public void updateToggleButton(boolean isLocked) {
        if (isLocked) {
            btnToggle.setText("Mở");
            btnToggle.setStyle("-fx-background-color: green; " +
                    "-fx-background-radius: 15; -fx-border-radius: 15;");
        } else {
            btnToggle.setText("Khóa");
            btnToggle.setStyle("-fx-background-color: red; " +
                    "-fx-background-radius: 15; -fx-border-radius: 15;");
        }
    }

    private String translateRole(String role) {
        return switch (role) {
            case "ADMIN"  -> "Quản trị viên";
            case "SELLER" -> "Người bán";
            case "BIDDER" -> "Người mua";
            default -> role;
        };
    }
}