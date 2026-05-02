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
    @FXML private TextField txtIDCard;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;

    @FXML private Label lblUsernameMessage;
    @FXML private Label lblPasswordMessage1;
    @FXML private Label lblPasswordMessage2;
    @FXML private Label lblIDCardMessage;
    @FXML private Label lblEmailMessage;
    @FXML private Label lblPhoneMessage;
    private boolean Password;
    private boolean Repassword;
    private boolean emailIsValid;
    private boolean IDCardIsValid;
    private boolean phoneIsValid;
    private boolean userIsValid;

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
        //Set up label
        if (lblUsernameMessage != null) {
            setUpLabel(lblUsernameMessage);
        }
        if (lblPasswordMessage1 != null) {
            setUpLabel(lblPasswordMessage1);
        }
        if (lblPasswordMessage2 != null) {
            setUpLabel(lblPasswordMessage2);
        }
        if(lblIDCardMessage != null){
            setUpLabel(lblIDCardMessage);
        }
        if(lblEmailMessage != null){
            setUpLabel(lblEmailMessage);
        }
        if(lblPhoneMessage != null){
            setUpLabel(lblPhoneMessage);
        }

        //Check sđt
        if(txtPhone != null){
            txtPhone.textProperty().addListener((obs, oldVal, newVal) -> {
                String phone = txtPhone.getText();
                if(phone != null) {
                    if (Validator.isValidPhone(phone)) {
                        updateLabel(lblPhoneMessage, "✅ Số điện thoại hợp lệ!", "green");
                        phoneIsValid = true;
                    } else {
                        updateLabel(lblPhoneMessage, "❌ Số điện thoại không hợp lệ!", "red");
                        phoneIsValid = false;
                    }
                }
                else {
                    setUpLabel(lblPhoneMessage);
                }
            });
        }

        //Check email
        if(txtEmail != null){
            txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
                String email = txtEmail.getText();
                if (email != null){
                    if (Validator.isValidEmail(email)) {
                        updateLabel(lblEmailMessage, "✅ Email hợp lệ!", "green");
                        emailIsValid = true;
                    } else {
                        updateLabel(lblEmailMessage, "❌ Email không hợp lệ!", "red");
                        emailIsValid = false;
                    }
                }
                else{
                    setUpLabel(lblEmailMessage);
                }
            });
        }

        //Check CCCD
        if(txtIDCard != null){
            txtIDCard.textProperty().addListener((obs, odlVal, newVal) -> {
                String IDCard = txtIDCard.getText();
                if(IDCard != null) {
                    if (Validator.isValidCCCD(IDCard)) {
                        updateLabel(lblIDCardMessage, "✅ CCCD hợp lệ!", "green");
                        IDCardIsValid = true;
                    } else {
                        updateLabel(lblIDCardMessage, "❌ CCCD không hợp lệ!", "red");
                        IDCardIsValid = false;
                    }
                }
                else {
                    setUpLabel(lblIDCardMessage);
                }
            });
        }

        //Check tên đăng nhập
        if(txtUsername != null){
            txtUsername.textProperty().addListener((obs, odlVal, newVal) -> {
                String user = txtUsername.getText();
                if(user != null) {
                    if (Validator.isValidUsername(user)) {
                        updateLabel(lblUsernameMessage, "✅ Tên đăng nhập hợp lệ!", "green");
                        userIsValid = true;
                    } else {
                        updateLabel(lblUsernameMessage, "❌ Tên đăng nhập không được chứa dấu, khoảng trắng hoặc kí tự đặc biệt. ", "red");
                        userIsValid = false;
                    }
                }
                else{
                    setUpLabel(lblUsernameMessage);
                }
            });
        }


        //Check mật khẩu mạnh và khớp
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