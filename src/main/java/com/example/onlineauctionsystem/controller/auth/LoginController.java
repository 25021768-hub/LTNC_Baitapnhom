package com.example.onlineauctionsystem.controller.auth;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.model.Account;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;


public class LoginController extends BaseController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblLoginMessage;
    @FXML private Button btnLogin;


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
        String user = txtUsername.getText();
        String password = txtPassword.getText();

        DataStorage.currentAccount = DataStorage.checkLogin(user, password);
        if(DataStorage.currentAccount != null){
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            showAlert("Đăng nhập", "Đăng nhập thành công.");
            switchScene(stage, SceneConfig.HOME);
        }
        else{
            updateLabel(lblLoginMessage, "Sai tên đăng nhập hoặc mật khẩu.", "red");
        }
    }

    @Override
    public void initialize() {
        // 1. Ràng buộc nút Đăng nhập: Chỉ sáng khi cả 2 ô không trống
        btnLogin.disableProperty().bind(
                txtUsername.textProperty().isEmpty()
                        .or(txtPassword.textProperty().isEmpty())
        );

        // 2. Lắng nghe phím Enter
        txtPassword.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (!btnLogin.isDisable()) {
                    onLogin(new ActionEvent(txtPassword, null));
                }
            }
        });
        Platform.runLater(() -> txtUsername.requestFocus());
    }
}




