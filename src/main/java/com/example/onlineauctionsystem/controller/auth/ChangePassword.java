package com.example.onlineauctionsystem.controller.auth;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.controller.ValidatorHelp;
import com.example.onlineauctionsystem.model.RemoteDataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import com.example.onlineauctionsystem.utils.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class ChangePassword extends BaseController {

    @FXML private Label lblCurrentMessage, lblReMessage, lblNewMessage;
    @FXML private PasswordField txtNewPassword, txtCurrentPassword, txtReNewPassword;
    @FXML private Button btnConfirm;

    @Override
    public void initialize() {
        btnConfirm.setDisable(true);
        txtCurrentPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            String oldPassword = RemoteDataStorage.currentAccount.getPassword();
            if (newValue.trim().equals(oldPassword)) {
                ValidatorHelp.updateLabel(lblCurrentMessage, "Khớp với mật khẩu hiện tại.", "green");
            } else {
                ValidatorHelp.updateLabel(lblCurrentMessage, "Không khớp với mật khẩu hiện tại.", "red");
            }
            checkConfirm();
        });
        ValidatorHelp.setupPasswordValidation(txtNewPassword, txtReNewPassword, lblNewMessage, lblReMessage, Validator::isValidPassword, this::checkConfirm);

    }

    private void checkConfirm(){
        boolean isValid = ValidatorHelp.isAllValid(lblNewMessage, lblCurrentMessage, lblReMessage);
        btnConfirm.setDisable(!isValid);
    }
    @FXML
    private void onReturnProfile(ActionEvent event) {
        String username = RemoteDataStorage.currentAccount.getUsername();
        if(RemoteDataStorage.changePassword(username, txtCurrentPassword.getText(), txtNewPassword.getText())){
            // BUG G: Hardcode BIDDER_HOME — SELLER cũng bị chuyển về trang bidder.
            // Dùng role để điều hướng đúng trang chủ.
            String role = RemoteDataStorage.currentAccount.getRole() != null
                    ? RemoteDataStorage.currentAccount.getRole().toUpperCase().trim() : "";
            com.example.onlineauctionsystem.utils.SceneConfig home =
                    "SELLER".equals(role)
                            ? com.example.onlineauctionsystem.utils.SceneConfig.SELLER_HOME
                            : com.example.onlineauctionsystem.utils.SceneConfig.BIDDER_HOME;
            switchScene(event, home);
            showAlert("Đổi mật khẩu", "Đổi mật khẩu thành công.");
        }
        else{
            showAlert("Đổi mật khẩu","Lỗi hệ thống.");
        }
    }
}