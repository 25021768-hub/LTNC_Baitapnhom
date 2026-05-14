package com.example.onlineauctionsystem.controller;

import com.example.onlineauctionsystem.controller.auth.RegisterController;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import com.example.onlineauctionsystem.utils.Validator;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Predicate;


public abstract class BaseController{
    protected void switchScene(ActionEvent event, String fxmlPath, String title){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Hàm 1: Nhận ActionEvent (Dùng cho nút bấm)
    protected void switchScene(ActionEvent event, SceneConfig sceneType) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        switchScene(stage, sceneType);
    }

    // Hàm 2: Nhận Stage trực tiếp (Dùng cho phím Enter hoặc chuyển trang chủ động)
    protected void switchScene(Stage stage, SceneConfig sceneType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(sceneType.getPath()));
            Scene scene = new Scene(loader.load());
            stage.setTitle(sceneType.getTitle());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void setUpLabel(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    protected void updateLabel(Label label, String text, String color) {
        label.setText(text);
        label.setStyle("-fx-text-fill: " + color + ";");
        label.setVisible(true);
        label.setManaged(true);
    }

    protected void setupValidation(TextField field, Label label,
                                            String originalValue, // Truyền null nếu là Đăng ký
                                            Predicate<String> formatChecker,
                                            String errorFormat, String successMsg,
                                            Runnable updateStatus) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (originalValue != null && newVal.equals(originalValue)) {
                setUpLabel(label);
            }
            else if (newVal == null || newVal.isEmpty()) {
                setUpLabel(label);
            }
            else {
                if (!formatChecker.test(newVal)) {
                    updateLabel(label, errorFormat, "red");
                } else {
                    if (originalValue == null || !newVal.equals(originalValue)) {
                        if (DataStorage.isAccountExists(newVal)) {
                            updateLabel(label, "Đã tồn tại trên hệ thống!", "red");
                        } else {
                            updateLabel(label, successMsg, "green");
                        }
                    } else {
                        setUpLabel(label);
                    }
                }
            }
            if (updateStatus != null) updateStatus.run();
        });
    }


    protected void setupPasswordValidation(PasswordField txtPass, PasswordField txtRePass,
                                           Label lblMsg1, Label lblMsg2,
                                           Predicate<String> strengthChecker,
                                           Runnable updateStatus) {

        javafx.beans.value.ChangeListener<String> passListener = (obs, old, newVal) -> {
            String p1 = txtPass.getText();
            String p2 = txtRePass.getText();

            // Check độ mạnh của mật khẩu chính
            if (p1.isEmpty()) {
                setUpLabel(lblMsg1);
            } else {
                boolean isStrong = strengthChecker.test(p1);
                updateLabel(lblMsg1, isStrong ? "Mật khẩu mạnh!" : "Yếu (8 ký tự, 1 hoa, 1 đặc biệt)",
                        isStrong ? "green" : "red");
            }

            // Check trùng khớp với ô nhập lại
            if (p2.isEmpty()) {
                setUpLabel(lblMsg2);
            } else {
                boolean isMatch = p1.equals(p2);
                updateLabel(lblMsg2, isMatch ? "Trùng khớp!" : "Mật khẩu không khớp!",
                        isMatch ? "green" : "red");
            }

            if (updateStatus != null) updateStatus.run();
        };

        txtPass.textProperty().addListener(passListener);
        txtRePass.textProperty().addListener(passListener);
    }

    protected boolean isAllValid(Label... labels) {
        for (Label lbl : labels) {
            if (lbl.getText().isEmpty() || lbl.getStyle().contains("red")) {
                return false;
            }
        }
        return true;
    }


    // Hàm initialize abstract để các con bắt buộc phải triển khai
    @FXML
    public abstract void initialize();
}
