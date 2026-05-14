package com.example.onlineauctionsystem.controller;

import com.example.onlineauctionsystem.model.Account;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import com.example.onlineauctionsystem.utils.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class UserProfile extends BaseController{
    @FXML private Label lblUsernameMessage, lblEmailMessage, lblPhoneMessage, lblIDCardMessage, lblName;
    private Account acc = DataStorage.currentAccount;
    @FXML private Label lblBalance;
    @FXML private TextField txtUsername, txtEmail, txtPhoneNumber, txtIDCard;
    @FXML private Button btnSave;

    @Override
    public void initialize() {
        btnSave.setDisable(true);
        txtUsername.setText(DataStorage.currentAccount.getUsername());
        txtEmail.setText(DataStorage.currentAccount.getEmail());
        txtIDCard.setText(DataStorage.currentAccount.getIdCard());
        txtPhoneNumber.setText(DataStorage.currentAccount.getPhoneNumber());
        lblBalance.setText(String.valueOf(DataStorage.currentAccount.getBalance()));
        lblName.setText(DataStorage.currentAccount.getUsername());

        setupValidation(txtPhoneNumber, lblPhoneMessage, acc.getUsername(), Validator::isValidPhone, "Số điện thoại không hợp lệ.", "Số điện thoại hợp lệ.", this::checkSave);

        setupValidation(txtEmail, lblEmailMessage, acc.getEmail(), Validator::isValidEmail, "Email không hợp lệ.", "Email hợp lệ.", this::checkSave);

        setupValidation(txtIDCard, lblIDCardMessage, acc.getIdCard(), Validator::isValidCCCD, "CCCD không hợp lệ.", "CCCD hợp lệ", this::checkSave);

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
                if(DataStorage.updateBalance(acc.getUsername(), amount)){
                    double newBalance = acc.getBalance() + amount;
                    showAlert("Nạp thành công", "Nạp thành công " + amountStr + " vào tài khoản");
                    DataStorage.currentAccount.setBalance(newBalance);
                    lblBalance.setText(String.valueOf(newBalance));
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
        if(DataStorage.updateAccount(acc)){
            showAlert("Đổi thông tin", "Đổi thành công.");
        }
        else{
            showAlert("Đổi thông tin", "Lỗi!");
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        Account current = DataStorage.currentAccount;

        // Gán lại dữ liệu cũ vào các ô text
        txtUsername.setText(current.getUsername());
        txtEmail.setText(current.getEmail());
        txtPhoneNumber.setText(current.getPhoneNumber());
        txtIDCard.setText(current.getIdCard());

        Label[] labels = {lblEmailMessage, lblPhoneMessage, lblIDCardMessage};
        for (Label l : labels){
            setUpLabel(l);
        }

        // Tắt nút Lưu
        btnSave.setDisable(true);
    }

    private void checkSave(){
         boolean isUpdate = false;
         if (lblPhoneMessage.getStyle().contains("green") && !(txtPhoneNumber.getText().trim().equals(acc.getPhoneNumber())) ){
             isUpdate = true;
             acc.setPhoneNumber(txtPhoneNumber.getText());
         }
         if (lblEmailMessage.getStyle().contains("green") && !(txtEmail.getText().trim().equals(acc.getEmail())) ){
             isUpdate = true;
             acc.setEmail(txtEmail.getText());
         }
         if (lblIDCardMessage.getStyle().contains("green") && !(txtIDCard.getText().trim().equals(acc.getIdCard())) ){
             isUpdate = true;
             acc.setIdCard(txtIDCard.getText());
         }
        btnSave.setDisable(!isUpdate);
    }

    @FXML
    private void onNewProduct(ActionEvent event) {
        switchScene(event, SceneConfig.BIDDER_NEW_PRODUCT);
    }
    @FXML
    private void onHistory(ActionEvent event) {
        switchScene(event, SceneConfig.BIDDER_HISTORY);
    }
    @FXML
    private void onActive(ActionEvent event) {
        switchScene(event, SceneConfig.BIDDER_ACTIVE);
    }
    @FXML
    private void onAccount(ActionEvent event) {
        switchScene(event, SceneConfig.HOME);
    }

}