package com.example.onlineauctionsystem.controller;

import com.example.onlineauctionsystem.model.RemoteDataStorage;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.function.Predicate;

public class ValidatorHelp{
    public static void setupValidation(TextField field, Label label,
                                       String originalValue,
                                       Predicate<String> formatChecker,
                                       String errorFormat, String successMsg,
                                       Runnable updateStatus) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (originalValue != null && newVal.equals(originalValue)) {
                setUpLabel(label);
            } else if (newVal == null || newVal.isEmpty()) {
                setUpLabel(label);
            } else {
                if (!formatChecker.test(newVal)) {
                    updateLabel(label, errorFormat, "red");
                    if (updateStatus != null) updateStatus.run();
                } else {
                    if (originalValue == null || !newVal.equals(originalValue)) {
                        // BUG D: isAccountExists() là network call — KHÔNG gọi trên UI thread.
                        // Chạy trên background thread, cập nhật label qua Platform.runLater().
                        final String valueToCheck = newVal;
                        Thread t = new Thread(() -> {
                            boolean exists = RemoteDataStorage.isAccountExists(valueToCheck);
                            javafx.application.Platform.runLater(() -> {
                                // Guard: chỉ cập nhật nếu text vẫn là giá trị đang check
                                if (!valueToCheck.equals(field.getText())) return;
                                if (exists) {
                                    updateLabel(label, "Đã tồn tại trên hệ thống!", "red");
                                } else {
                                    updateLabel(label, successMsg, "green");
                                }
                                if (updateStatus != null) updateStatus.run();
                            });
                        });
                        t.setDaemon(true);
                        t.start();
                        return; // updateStatus sẽ được gọi trong runLater ở trên
                    } else {
                        setUpLabel(label);
                    }
                }
            }
            if (updateStatus != null) updateStatus.run();
        });
    }


    public static void setupPasswordValidation(PasswordField txtPass, PasswordField txtRePass,
                                               Label lblMsg1, Label lblMsg2,
                                               Predicate<String> strengthChecker,
                                               Runnable updateStatus) {

        javafx.beans.value.ChangeListener<String> passListener = (obs, old, newVal) -> {
            String p1 = txtPass.getText();
            String p2 = txtRePass.getText();

            // Check độ mạnh của mật khẩu chính
            if (p1.isEmpty()) {
                setUpLabel(lblMsg1);
            } else {
                boolean isStrong = strengthChecker.test(p1);
                updateLabel(lblMsg1, isStrong ? "Mật khẩu mạnh!" : "Yếu (8 ký tự, 1 hoa, 1 đặc biệt)",
                        isStrong ? "green" : "red");
            }

            // Check trùng khớp với ô nhập lại
            if (p2.isEmpty()) {
                setUpLabel(lblMsg2);
            } else {
                boolean isMatch = p1.equals(p2);
                updateLabel(lblMsg2, isMatch ? "Trùng khớp!" : "Mật khẩu không khớp!",
                        isMatch ? "green" : "red");
            }

            if (updateStatus != null) updateStatus.run();
        };

        txtPass.textProperty().addListener(passListener);
        txtRePass.textProperty().addListener(passListener);
    }

    public static void setUpLabel(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    public static void updateLabel(Label label, String text, String color) {
        label.setText(text);
        label.setStyle("-fx-text-fill: " + color + ";");
        label.setVisible(true);
        label.setManaged(true);
    }

    public static boolean isAllValid(Label... labels) {
        for (Label lbl : labels) {
            // BUG C: Logic cũ return false khi text rỗng (label đang ẩn) — sai.
            // Chỉ fail khi label đang HIỂN THỊ và có màu đỏ (lỗi thực sự).
            // Label ẩn (setVisible(false)) = chưa nhập / chưa thay đổi → không tính là invalid.
            if (lbl.isVisible() && lbl.getStyle().contains("red")) {
                return false;
            }
            // Nếu tất cả label đều ẩn → không có field nào hợp lệ (người dùng chưa nhập gì)
            // Kiểm tra phải có ít nhất 1 label xanh
        }
        // Phải có ít nhất 1 label hiển thị màu xanh để kích hoạt nút
        for (Label lbl : labels) {
            if (lbl.isVisible() && lbl.getStyle().contains("green")) {
                return true;
            }
        }
        return false;
    }
}