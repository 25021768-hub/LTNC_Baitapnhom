package com.example.onlineauctionsystem.controller.auth;

import com.example.onlineauctionsystem.controller.BaseController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.example.onlineauctionsystem.utils.Validator;

public class LoginController extends BaseController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;


    // Chuyển màn hình đăng kí
    @FXML
    private void onRegisterClick(ActionEvent event){
        switchScene(event, "/com/example/onlineauctionsystem/Dang_Ky.fxml", "Đăng kí tài khoản");
    }

    //Quên mật khẩu
    @FXML
    private void onForgotPassword(ActionEvent event){
        switchScene(event, "/com/example/onlineauctionsystem/Quen_Mat_Khau_BTL.fxml", "Quên mật khẩu");
    }

    //Đăng nhập
    @FXML
    private void onLogin(ActionEvent event){
        if(txtUsername.getText().trim().isEmpty() && txtPassword.getText().trim().isEmpty()){
            if()
            switchScene(event, "/com/example/onlineauctionsystem/", "Trang chủ");
        }
    }
}




