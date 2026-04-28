package com.example.onlineauctionsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController extends BaseController{
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtRePassword;
    @FXML private Label lblUsernameMessage;
    @FXML private Label lblPasswordMessage1;
    @FXML private Label lblPasswordMessage2;
    @FXML private Hyperlink btnGoToRegister;

    @FXML
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    //---------- PHẦN ĐĂNG KÍ-------------
    // Chuyển màn hình đăng kí
    @FXML
    private void onRegisterClick(ActionEvent event){
        switchScene(event, "/com/example/onlineauctionsystem/Dang_Ky.fxml", "Đăng kí tài khoản");
    }

    //Chuyển về màn hình đăng nhập
    @FXML
    private void onReturnLogin(ActionEvent event){
        switchScene(event, "/com/example/onlineauctionsystem/Dang_Nhap_BTL.fxml", "Đăng nhập tài khoản");
    }

    //Set label ẩn đi
    private void setUpLabel(Label label){
        label.setVisible(false);
        label.setManaged(false);
    }

    //Khời tạo
    @FXML
    private void initialize(){
        if (lblUsernameMessage != null) setUpLabel(lblUsernameMessage);
        if (lblPasswordMessage1 != null) setUpLabel(lblPasswordMessage1);
        if (lblPasswordMessage2 != null) setUpLabel(lblPasswordMessage2);
    }
    //---------PHẦN ĐĂNG NHẬP----------
    @FXML
    private void onForgotPassword(ActionEvent event){
        switchScene(event, "/com/example/onlineauctionsystem/Quen_Mat_Khau_BTL.fxml", "Quên mật khẩu");
    }

    //--------Phần quên mật khẩu---------

}




