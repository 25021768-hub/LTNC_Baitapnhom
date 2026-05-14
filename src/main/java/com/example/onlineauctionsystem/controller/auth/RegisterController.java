package com.example.onlineauctionsystem.controller.auth;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.model.Account;
import com.example.onlineauctionsystem.model.AuctionMessage;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
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

    private boolean roleIsValid;

    private void checkRegister() {
        boolean isValid = isAllValid(lblUsernameMessage, lblPasswordMessage1,
                lblPasswordMessage2, lblEmailMessage,
                lblPhoneMessage, lblIDCardMessage);

        // Kết hợp thêm điều kiện Checkbox và ComboBox
        boolean canSubmit = isValid && cbCheck.isSelected() && cbRole.getValue() != null;

        btnRegister.setDisable(!canSubmit);
    }

    @FXML
    public void initialize() {
        // Ẩn tất cả label lúc mới vào
        Label[] labels = {lblUsernameMessage, lblPasswordMessage1, lblPasswordMessage2, lblIDCardMessage, lblEmailMessage, lblPhoneMessage};
        for (Label l : labels) {
            if (l != null) setUpLabel(l);
        }

        txtUsername.requestFocus();

        // Check SĐT + Database
        setupValidation(txtPhone, lblPhoneMessage, null, Validator::isValidPhone, "Số điện thoại không hợp lệ.", "Số điện thoại hợp lệ.", this::checkRegister);

        // Check Email + Database
        setupValidation(txtEmail, lblEmailMessage, null, Validator::isValidEmail, "Email không hợp lệ.", "Email hợp lệ.", this::checkRegister);

        // Check CCCD + Database
        setupValidation(txtIDCard, lblIDCardMessage, null, Validator::isValidCCCD, "CCCD không hợp lệ.", "CCCD hợp lệ", this::checkRegister);

        // Check Username + Database
        setupValidation(txtUsername, lblUsernameMessage, null, Validator::isValidUsername, "Tên đăng nhập không được dấu, khoảng trắng và kí tự đặc biệt.", "Tên hơp lệ", this::checkRegister);

        // Check đã tích chưa
        cbCheck.selectedProperty().addListener((o, old, newVal) -> checkRegister());

        //Check đã chọn vai trò chưa
        cbRole.valueProperty().addListener((o, old, newVal) -> {
            roleIsValid = (newVal != null && !newVal.toString().isEmpty());
            checkRegister();
        });

        // Password Listener
        setupPasswordValidation(txtPassword, txtRePassword, lblPasswordMessage1, lblPasswordMessage2, Validator::isValidPassword, this::checkRegister);
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
            switchScene(event, SceneConfig.LOGIN);
        } else {
            showAlert("Đăng kí", "Lỗi hệ thống!");
        }
    }

    @FXML
    private void onReturnLogin(ActionEvent event) {
        switchScene(event, SceneConfig.LOGIN);
    }
}