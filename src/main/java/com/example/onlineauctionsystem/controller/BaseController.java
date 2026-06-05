package com.example.onlineauctionsystem.controller;

import com.example.onlineauctionsystem.controller.auth.RegisterController;
import com.example.onlineauctionsystem.model.RemoteDataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import com.example.onlineauctionsystem.utils.Validator;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    public String formatPrice(double price) {
        return String.format("%,.0fđ", price).replace(",", ".");
    }

    public void forceLogout(ActionEvent event) {
        showAlert("Tài khoản bị khóa", "Tài khoản của bạn đã bị Quản trị viên khóa! Hệ thống sẽ tự động đăng xuất.");
        RemoteDataStorage.currentAccount = null;

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        switchScene(stage, SceneConfig.LOGIN);
    }
    // Hàm initialize abstract để các con bắt buộc phải triển khai
    @FXML
    public abstract void initialize();
}