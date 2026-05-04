package com.example.onlineauctionsystem.controller.auth;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import com.example.onlineauctionsystem.utils.Validator;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ForgotPasswordController extends BaseController {

    @FXML private Button btnComplete;
    @FXML private TextField txtFind;
    @FXML private PasswordField txtNewPassword, txtReNewPassword;
    @FXML private Label lblMessage, lblPasswordMessage1, lblPasswordMessage2;
    private boolean password, repassword;
    private static String identify;

    //Trở lại đăng nhập
    @FXML
    private void onReturnLogin(ActionEvent event){
        switchScene(event, "/com/example/onlineauctionsystem/Dang_Nhap_BTL.fxml", "Đăng nhập tài khoản");
    }
    //Tìm tài khoản
    @FXML
    private void onFindAccount(ActionEvent event) {
        String identifier = txtFind.getText().trim();
        if (DataStorage.isAccountExists(identifier)) {
            identify = identifier;
            showAlert("Quên mật khẩu", "Tìm thấy tài khoản! Vui lòng đặt mật khẩu mới.");
            switchScene(event, "/com/example/onlineauctionsystem/Dat_Lai_Mat_Khau_BTL.fxml", "Đổi mật khẩu");
        } else {
            updateLabel(lblMessage, "Thông tin không chính xác hoặc tài khoản chưa đăng ký!", "red");
        }
    }
    @FXML
    private void initialize() {
        if(txtNewPassword != null && txtReNewPassword != null) {
            Label[] labels = {lblPasswordMessage1, lblPasswordMessage2};
            for (Label l : labels) {
                if (l != null) setUpLabel(l);
            }
            ChangeListener<String> passwordListener = (obs, oldVal, newVal) -> {
                String p1 = txtNewPassword.getText();
                String p2 = txtReNewPassword.getText();
                password = Validator.isValidPassword(p1);
                if (p1.isEmpty()) setUpLabel(lblPasswordMessage1);
                else
                    updateLabel(lblPasswordMessage1, password ? "Mật khẩu mạnh!" : "Cần ít nhất 8 kí tự, 1 hoa, 1 đặc biệt.", password ? "green" : "red");

                repassword = !p2.isEmpty() && p2.equals(p1);
                if (p2.isEmpty()) setUpLabel(lblPasswordMessage2);
                else
                    updateLabel(lblPasswordMessage2, repassword ? "Trùng khớp!" : "Mật khẩu không trùng khớp!", repassword ? "green" : "red");
                checkValid();
            };
            txtNewPassword.textProperty().addListener(passwordListener);
            txtReNewPassword.textProperty().addListener(passwordListener);
        }
    }

    //Vô hiệu hóa nút đổi mật khẩu
    private void checkValid(){
        boolean allValid = password && repassword;
        btnComplete.setDisable(!allValid);
    }


    @FXML
    private void onChangeForgotPassword(ActionEvent event){
        if(DataStorage.changeForgotPassword(identify, txtNewPassword.getText())) {
            showAlert("Quên mật khẩu", "Đổi mật khẩu thành công!");
            switchScene(event, SceneConfig.LOGIN);
        }
        else{
            showAlert("Quên mật khẩu", "Lỗi hệ thống!");
        }
    }

}