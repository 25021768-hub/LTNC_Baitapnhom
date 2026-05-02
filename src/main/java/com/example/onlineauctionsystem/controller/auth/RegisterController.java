package com.example.onlineauctionsystem.controller.auth;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.model.Account;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.Validator;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController extends BaseController {
    @FXML private CheckBox cbCheck;
    @FXML private ComboBox<String> cbRole;
    @FXML private Button btnRegister;
    @FXML private TextField txtUsername, txtIDCard, txtEmail, txtPhone;
    @FXML private PasswordField txtPassword, txtRePassword;
    @FXML private Label lblUsernameMessage, lblPasswordMessage1, lblPasswordMessage2, lblIDCardMessage, lblEmailMessage, lblPhoneMessage;

    private boolean Password, Repassword, emailIsValid, IDCardIsValid, phoneIsValid, userIsValid, roleIsValid;

    @FXML
    private void setUpLabel(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }


    private void checkRegister() {
        boolean allValid = (Password && Repassword && emailIsValid && IDCardIsValid && phoneIsValid && userIsValid && roleIsValid && cbCheck.isSelected());
        btnRegister.setDisable(!allValid);
    }

    @FXML
    private void initialize() {
        // Ẩn tất cả label lúc mới vào
        Label[] labels = {lblUsernameMessage, lblPasswordMessage1, lblPasswordMessage2, lblIDCardMessage, lblEmailMessage, lblPhoneMessage};
        for (Label l : labels) { if (l != null) setUpLabel(l); }

        txtUsername.requestFocus();

        // Check SĐT + Database
        txtPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            phoneIsValid = Validator.isValidPhone(newVal);
            if (newVal == null || newVal.isEmpty()) setUpLabel(lblPhoneMessage);
            else {
                if (phoneIsValid) {
                    if (!DataStorage.isAccountExists(newVal)) updateLabel(lblPhoneMessage, "✅ Số điện thoại hợp lệ!", "green");
                    else { updateLabel(lblPhoneMessage, "❌ Số điện thoại đã tồn tại!", "red"); phoneIsValid = false; }
                } else updateLabel(lblPhoneMessage, "❌ Số điện thoại không hợp lệ!", "red");
            }
            checkRegister();
        });

        // Check Email + Database
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            emailIsValid = Validator.isValidEmail(newVal);
            if (newVal == null || newVal.isEmpty()) setUpLabel(lblEmailMessage);
            else {
                if (emailIsValid) {
                    if (!DataStorage.isAccountExists(newVal)) updateLabel(lblEmailMessage, "✅ Email hợp lệ!", "green");
                    else { updateLabel(lblEmailMessage, "❌ Email đã tồn tại!", "red"); emailIsValid = false; }
                } else updateLabel(lblEmailMessage, "❌ Email không hợp lệ!", "red");
            }
            checkRegister();
        });

        // Check CCCD + Database
        txtIDCard.textProperty().addListener((obs, oldVal, newVal) -> {
            IDCardIsValid = Validator.isValidCCCD(newVal);
            if (newVal == null || newVal.isEmpty()) { setUpLabel(lblIDCardMessage); IDCardIsValid = false; }
            else {
                if (IDCardIsValid) {
                    if (!DataStorage.isAccountExists(newVal)) updateLabel(lblIDCardMessage, "✅ CCCD hợp lệ!", "green");
                    else { updateLabel(lblIDCardMessage, "❌ CCCD đã tồn tại!", "red"); IDCardIsValid = false; }
                } else updateLabel(lblIDCardMessage, "❌ CCCD không hợp lệ!", "red");
            }
            checkRegister();
        });

        // Check Username + Database
        txtUsername.textProperty().addListener((obs, oldVal, newVal) -> {
            userIsValid = Validator.isValidUsername(newVal);
            if (newVal == null || newVal.isEmpty()) setUpLabel(lblUsernameMessage);
            else {
                if (userIsValid) {
                    if (!DataStorage.isAccountExists(newVal)) updateLabel(lblUsernameMessage, "✅ Tên đăng nhập hợp lệ!", "green");
                    else { updateLabel(lblUsernameMessage, "❌ Tên đăng nhập đã tồn tại!", "red"); userIsValid = false; }
                } else updateLabel(lblUsernameMessage, "❌ Tên không chứa dấu/khoảng trắng!", "red");
            }
            checkRegister();
        });

        // Check đã tích chưa
        cbCheck.selectedProperty().addListener((o, old, newVal) -> checkRegister());

        //Check đã chọn vai trò chưa
        cbRole.valueProperty().addListener((o, old, newVal) -> {
            roleIsValid = (newVal != null && !newVal.toString().isEmpty());
            checkRegister();
        });

        // Password Listener
        ChangeListener<String> passwordListener = (o, old, newVal) -> {
            String p1 = txtPassword.getText();
            String p2 = txtRePassword.getText();
            Password = Validator.isValidPassword(p1);
            if (p1.isEmpty()) setUpLabel(lblPasswordMessage1);
            else updateLabel(lblPasswordMessage1, Password ? "✅ Mật khẩu mạnh!" : "❌ Cần ít nhất 8 kí tự, 1 hoa, 1 đặc biệt.", Password ? "green" : "red");

            Repassword = !p2.isEmpty() && p2.equals(p1);
            if (p2.isEmpty()) setUpLabel(lblPasswordMessage2);
            else updateLabel(lblPasswordMessage2, Repassword ? "✅ Trùng khớp!" : "❌ Mật khẩu không trùng khớp!", Repassword ? "green" : "red");
            checkRegister();
        };
        txtPassword.textProperty().addListener(passwordListener);
        txtRePassword.textProperty().addListener(passwordListener);
    }

    //Tạo tài khoản mới
    @FXML
    private void registerNewUser(ActionEvent event) {
        Account acc = new Account(
                txtUsername.getText(),
                txtPassword.getText(),
                cbRole.getValue().toString(),
                txtIDCard.getText(),
                txtEmail.getText(),
                txtPhone.getText()
        );
        if (DataStorage.register(acc)) {
            showAlert("Đăng kí", "Đăng kí tài khoản thành công!");
            switchScene(event, "/com/example/onlineauctionsystem/Dang_Nhap_BTL.fxml", "Đăng nhập");
        } else {
            showAlert("Đăng kí", "Lỗi hệ thống!");
        }
    }

    @FXML
    private void onReturnLogin(ActionEvent event) {
        switchScene(event, "/com/example/onlineauctionsystem/Dang_Nhap_BTL.fxml", "Đăng nhập");
    }
}