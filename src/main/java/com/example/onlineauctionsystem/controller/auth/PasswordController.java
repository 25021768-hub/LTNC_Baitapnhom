package com.example.onlineauctionsystem.controller.auth;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.model.DataStorage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PasswordController extends BaseController {

    @FXML private TextField txtFind;
    @FXML private Label lblMessage;

    //Trở lại đăng nhập
    @FXML
    private void onReturnLogin(ActionEvent event){
        switchScene(event, "/com/example/onlineauctionsystem/Dang_Nhap_BTL.fxml", "Đăng nhập tài khoản");
    }

    @FXML
    private void onFindAccount(ActionEvent event) {
        String identifier = txtFind.getText().trim();
        if(DataStorage.isAccountExists(identifier)){
            showAlert("Quên mật khẩu", "Tìm thấy tài khoản! Vui lòng đặt mật khẩu mới.");
            switchScene(event, "/com/example/onlineauctionsystem/Dat_Lai_Mat_Khau_BTL.fxml", "Đổi mật khẩu");
        }
        else{
            updateLabel(lblMessage, "❌ Thông tin không chính xác hoặc tài khoản chưa đăng ký!", "red");
        }
    }
}