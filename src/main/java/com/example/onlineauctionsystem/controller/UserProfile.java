package com.example.onlineauctionsystem.controller;

import com.example.onlineauctionsystem.model.Account;
import com.example.onlineauctionsystem.model.RemoteDataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import com.example.onlineauctionsystem.utils.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class UserProfile extends MenuController{
    @FXML private Label lblFullNameMessage, lblEmailMessage, lblPhoneMessage, lblIDCardMessage, lblName;
    private Account acc = RemoteDataStorage.currentAccount;
    @FXML private Label lblBalance;
    @FXML private TextField txtFullName, txtEmail, txtPhoneNumber, txtIDCard;
    @FXML private Button btnSave;

    @Override
    public void initialize() {
        btnSave.setDisable(true);
        txtFullName.setText(RemoteDataStorage.currentAccount.getFullName());
        txtEmail.setText(RemoteDataStorage.currentAccount.getEmail());
        txtIDCard.setText(RemoteDataStorage.currentAccount.getIdCard());
        txtPhoneNumber.setText(RemoteDataStorage.currentAccount.getPhoneNumber());
        lblBalance.setText(formatPrice(RemoteDataStorage.currentAccount.getBalance()));
        lblName.setText(RemoteDataStorage.currentAccount.getFullName());

        ValidatorHelp.setupValidation(txtPhoneNumber, lblPhoneMessage, acc.getPhoneNumber(), Validator::isValidPhone, "Số điện thoại không hợp lệ.", "Số điện thoại hợp lệ.", this::checkSave);

        ValidatorHelp.setupValidation(txtEmail, lblEmailMessage, acc.getEmail(), Validator::isValidEmail, "Email không hợp lệ.", "Email hợp lệ.", this::checkSave);

        ValidatorHelp.setupValidation(txtIDCard, lblIDCardMessage, acc.getIdCard(), Validator::isValidCCCD, "CCCD không hợp lệ.", "CCCD hợp lệ", this::checkSave);

        ValidatorHelp.setupValidation(txtFullName, lblFullNameMessage, acc.getFullName(),
                name -> name != null && !name.trim().isEmpty(),
                "Họ tên không được để trống.", "Họ tên hợp lệ.", this::checkSave);
    }

    @FXML
    private void onDeposit(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nạp tiền vào tài khoản");
        dialog.setHeaderText("Số dư hiện tại:" + acc.getBalance());
        dialog.setContentText("Nhập số tiền muốn nạp:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
                    try{
                        double amount = Double.parseDouble(amountStr);
                        if(amount < 10000){
                            showAlert("Lỗi", "Số tiền nạp phải lớn hơn 10000.");
                            return;
                        }
                        if(RemoteDataStorage.updateBalance(acc.getUsername(), amount)){
                            double newBalance = acc.getBalance() + amount;
                            showAlert("Nạp thành công", "Nạp thành công " + amountStr + " vào tài khoản");
                            RemoteDataStorage.currentAccount.setBalance(newBalance);
                            lblBalance.setText(formatPrice(newBalance));
                        }
                        else{
                            showAlert("Lỗi", "Lỗi hệ thống.");
                        }
                    }
                    catch (NumberFormatException e){
                        showAlert("Lỗi", "Số tiền không hợp lệ.");
                    }
                }
        );
    }

    @FXML
    private void onChangePassword(ActionEvent event){
        switchScene(event, SceneConfig.CHANGE_PASSWORD);
    }

    @FXML
    private void onSave(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn với các thay đổi?");
        if(alert.showAndWait().get() == ButtonType.OK) {
            acc.setFullName(txtFullName.getText().trim());
            acc.setEmail(txtEmail.getText().trim());
            acc.setPhoneNumber(txtPhoneNumber.getText().trim());
            acc.setIdCard(txtIDCard.getText().trim());
            if (RemoteDataStorage.updateAccount(acc)) {
                showAlert("Đổi thông tin", "Đổi thành công.");

                lblName.setText(acc.getFullName());
                txtFullName.setText(acc.getFullName());
                txtEmail.setText(acc.getEmail());
                txtPhoneNumber.setText(acc.getPhoneNumber());
                txtIDCard.setText(acc.getIdCard());

                Label[] labels = {lblEmailMessage, lblPhoneMessage, lblIDCardMessage, lblFullNameMessage};
                for (Label l : labels) {
                    ValidatorHelp.setUpLabel(l);
                }

                btnSave.setDisable(true);
            } else {
                showAlert("Đổi thông tin", "Lỗi!");
            }
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        Account current = RemoteDataStorage.currentAccount;

        // Gán lại dữ liệu cũ vào các ô text
        txtFullName.setText(current.getFullName());
        txtEmail.setText(current.getEmail());
        txtPhoneNumber.setText(current.getPhoneNumber());
        txtIDCard.setText(current.getIdCard());

        Label[] labels = {lblFullNameMessage, lblEmailMessage, lblPhoneMessage, lblIDCardMessage};
        for (Label l : labels){
            ValidatorHelp.setUpLabel(l);
        }

        // Tắt nút Lưu
        btnSave.setDisable(true);
    }

    private void checkSave(){
        boolean isUpdate = false;
        if (lblPhoneMessage.getStyle().contains("green") && !(txtPhoneNumber.getText().trim().equals(acc.getPhoneNumber())) ){
            isUpdate = true;
        }
        if (lblEmailMessage.getStyle().contains("green") && !(txtEmail.getText().trim().equals(acc.getEmail())) ){
            isUpdate = true;;
        }
        if (lblIDCardMessage.getStyle().contains("green") && !(txtIDCard.getText().trim().equals(acc.getIdCard())) ){
            isUpdate = true;
        }
        if (lblFullNameMessage.getStyle().contains("green") && !(txtFullName.getText().trim().equals(acc.getFullName())) ){
            isUpdate = true;
        }
        Label[] labels = {lblPhoneMessage, lblEmailMessage, lblIDCardMessage, lblFullNameMessage};
        for (Label lbl : labels) {
            if (lbl != null && lbl.isVisible() && lbl.getStyle().contains("red")) {
                isUpdate = false;
                break;
            }
        }
        btnSave.setDisable(!isUpdate);
    }

    @FXML
    @Override
    public void onMyProducts(ActionEvent event) {
        super.onMyProducts(event);
    }

    @FXML
    @Override
    public void onHistory(ActionEvent event) {
        super.onHistory(event);
    }

    @FXML
    @Override
    public void onManage(ActionEvent event) {
        super.onManage(event);
    }

    @FXML
    @Override
    public void onAccount(ActionEvent event) {
        super.onAccount(event);
    }

}