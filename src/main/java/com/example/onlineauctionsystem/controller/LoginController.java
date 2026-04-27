package com.example.onlineauctionsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController extends BaseController{
    @FXML
    public Hyperlink btnGoToRegister;
    @FXML
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void onRegisterClick(ActionEvent event){
        switchScene(event, "/com/example/onlineauctionsystem/Dang_Ky.fxml", "Đăng kí tài khoản");
    }
}
