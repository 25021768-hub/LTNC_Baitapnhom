package com.example.onlineauctionsystem.controller.auth;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.model.Account;
import com.example.onlineauctionsystem.controller.ValidatorHelp;
import com.example.onlineauctionsystem.model.RemoteDataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
        String user = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Chạy đăng nhập trên background thread để không block UI
        btnLogin.disableProperty().unbind(); // tạm bỏ bind để có thể setDisable thủ công
        btnLogin.setDisable(true);
        Thread t = new Thread(() -> {
            Account acc = RemoteDataStorage.checkLogin(user, password);
            Platform.runLater(() -> {
                // Khôi phục bind sau khi xong
                btnLogin.disableProperty().bind(
                        txtUsername.textProperty().isEmpty()
                                .or(txtPassword.textProperty().isEmpty())
                );
                if (acc == null) {
                    String lastError = RemoteDataStorage.getLastLoginError();
                    if (lastError != null && lastError.contains("thiết bị khác")) {
                        ValidatorHelp.updateLabel(lblLoginMessage,
                                "⚠ Tài khoản đang đăng nhập ở thiết bị khác!\nVui lòng đợi người dùng kia đăng xuất.", "red");
                    } else if (lastError != null && lastError.contains("bị khóa")) {
                        ValidatorHelp.updateLabel(lblLoginMessage,
                                "🔒 Tài khoản đã bị khóa! Vui lòng liên hệ quản trị viên.", "red");
                    } else {
                        ValidatorHelp.updateLabel(lblLoginMessage, "Sai tên đăng nhập hoặc mật khẩu.", "red");
                    }
                    return;
                }

                RemoteDataStorage.currentAccount = acc;
                Stage stage = (Stage) btnLogin.getScene().getWindow();

                showAlert("Đăng nhập", "Đăng nhập thảnh công.");

                String role = acc.getRole() != null
                        ? acc.getRole().toUpperCase().trim()
                        : "GUEST";

                switch (role) {
                    case "BIDDER":
                        switchScene(stage, SceneConfig.BIDDER_HOME);
                        break;
                    case "SELLER":
                        switchScene(stage, SceneConfig.SELLER_HOME);
                        break;
                    case "ADMIN":
                        switchScene(stage, SceneConfig.ADMIN_USER);
                        break;
                    default:
                        showAlert("Lỗi phân quyền", "Tài khoản chưa được cấp quyền truy cập hệ thống.");
                        break;
                }
            });
        });
        t.setDaemon(true);
        t.start();
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