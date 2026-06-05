package com.example.onlineauctionsystem.controller.auth;

import com.example.onlineauctionsystem.controller.BaseController;
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

        RemoteDataStorage.currentAccount = RemoteDataStorage.checkLogin(user, password);
        if (RemoteDataStorage.currentAccount != null) {
            Stage stage = (Stage) btnLogin.getScene().getWindow();

            // THÊM BỘ KIỂM TRA TRẠNG THÁI KHÓA TÀI KHOẢN TẠI ĐÂY
            if (RemoteDataStorage.currentAccount.isLocked()) {
                showAlert("Đăng nhập thất bại", "Tài khoản của bạn đã bị khóa bởi Quản trị viên!");
                RemoteDataStorage.currentAccount = null; // Reset lại phiên đăng nhập để đảm bảo an toàn
                return; // Dừng hàm luôn, không cho chạy tiếp xuống phần phân quyền
            }

            // Nếu vượt qua bộ lọc khóa ở trên thì mới báo thành công và phân quyền
            showAlert("Đăng nhập", "Đăng nhập thành công.");

            String role = RemoteDataStorage.currentAccount.getRole() != null
                    ? RemoteDataStorage.currentAccount.getRole().toUpperCase().trim()
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
                    showAlert("Lỗi phân quyền", "Tài khoản của bạn chưa được cấp quyền truy cập hệ thống.");
                    break;
            }
        } else {
            ValidatorHelp.updateLabel(lblLoginMessage, "Sai tên đăng nhập hoặc mật khẩu.", "red");
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