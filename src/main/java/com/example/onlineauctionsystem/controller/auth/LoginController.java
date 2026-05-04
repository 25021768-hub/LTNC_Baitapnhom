package com.example.onlineauctionsystem.controller.auth;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;


public class LoginController extends BaseController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;


    // Chuyển màn hình đăng kí
    @FXML
    private void onRegisterClick(ActionEvent event){
        switchScene(event, SceneConfig.REGISTER);
    }

    //Quên mật khẩu
    @FXML
    private void onForgotPassword(ActionEvent event){
        switchScene(event, SceneConfig.FORGOT_PASSWORD);
    }

    //Đăng nhập
    @FXML
    private void onLogin(ActionEvent event){

    }
}




