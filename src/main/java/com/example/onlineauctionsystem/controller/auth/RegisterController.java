package com.example.onlineauctionsystem.controller.auth;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.utils.Validator;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController extends BaseController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtRePassword;
    @FXML private Label lblUsernameMessage;
    @FXML private Label lblPasswordMessage1;
    @FXML private Label lblPasswordMessage2;
    private boolean Password;
    private boolean Repassword;

    //---------- PHẦN ĐĂNG KÍ-------------

    //Set label
    @FXML
    private void setUpLabel(Label label){
        label.setVisible(false);
        label.setManaged(false);
    }
    private void updateLabel(Label label, String text, String color){
        label.setText(text);
        label.setStyle("-fx-text-fill: " + color + ";");
        label.setVisible(true);
        label.setManaged(true);
    }


    //Khời tạo
    @FXML
    private void initialize() {
        if (lblUsernameMessage != null) {
            setUpLabel(lblUsernameMessage);
            lblUsernameMessage.setWrapText(true);
        }
        if (lblPasswordMessage1 != null) {
            setUpLabel(lblPasswordMessage1);
            lblPasswordMessage1.setWrapText(true);
        }
        if (lblPasswordMessage2 != null) {
            setUpLabel(lblPasswordMessage2);
            lblPasswordMessage2.setWrapText(true);
        }

        if (txtPassword != null && txtRePassword != null) {
            ChangeListener<String> passwordListener = ((observable, oldValue, newValue) -> {
                String p1 = txtPassword.getText();
                String p2 = txtRePassword.getText();
                //----Kiểm tra mật khẩu mạnh------
                if (p1 == null || p1.isEmpty()){
                    if(lblPasswordMessage1 != null){setUpLabel(lblPasswordMessage1);}
                }
                else if (lblPasswordMessage1 != null){
                    if(Validator.isValidPassword(p1)){
                        updateLabel(lblPasswordMessage1, "✅ Mật khẩu rất mạnh!", "green");
                        Password = true;
                    }
                    else{
                        updateLabel(lblPasswordMessage1,"❌ Cần ít nhất 8 kí tự, 1 chữ hoa, 1 kí tự đặc biệt.", "red");
                        Password = false;
                    }
                }
                //-----Kiểm tra khớp mật khẩu-----
                if (p2 == null || p2.isEmpty()){
                    if(lblPasswordMessage2 != null) setUpLabel(lblPasswordMessage2);
                }
                else if (lblPasswordMessage2 != null){
                    if(p2.equals(p1)){
                        updateLabel(lblPasswordMessage2, "✅ Mật khẩu đã trùng khớp.", "green");
                        Repassword = true;
                    }
                    else{
                        updateLabel(lblPasswordMessage2, "❌ Mật khẩu không trùng khớp!", "red");
                        Repassword = false;
                    }
                }
            });
            txtPassword.textProperty().addListener(passwordListener);
            txtRePassword.textProperty().addListener(passwordListener);
        }
    }

    //Chuyển về màn hình đăng nhập
    @FXML
    private void onReturnLogin(ActionEvent event){
        switchScene(event, "/com/example/onlineauctionsystem/Dang_Nhap_BTL.fxml", "Đăng nhập tài khoản");
    }
}