package com.example.onlineauctionsystem.controller;

import com.example.onlineauctionsystem.model.Account;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class UserProfile extends BaseController{
    private Account acc = DataStorage.currentAccount;
    @FXML private Label lblBalance;
    @FXML private TextField txtUsername, txtEmail, txtPhoneNumber, txtIDCard;

    @Override
    public void initialize() {
        txtUsername.setText(DataStorage.currentAccount.getUsername());
        txtEmail.setText(DataStorage.currentAccount.getEmail());
        txtIDCard.setText(DataStorage.currentAccount.getIdCard());
        txtPhoneNumber.setText(DataStorage.currentAccount.getPhoneNumber());
        lblBalance.setText(String.valueOf(DataStorage.currentAccount.getBalance()));
    }

    @FXML
    private void onDeposit(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nạp tiền vào tài khoản");
        dialog.setHeaderText("Số dư hiện tại:" + acc.getUsername());
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
}