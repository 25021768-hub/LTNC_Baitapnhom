package com.example.onlineauctionsystem.controller;

import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public abstract class BidderSwitch extends BaseController {
    @FXML
    protected void onNewProduct(ActionEvent event) {
        switchScene(event, SceneConfig.BIDDER_NEW_PRODUCT);
    }
    @FXML
    protected void onHistory(ActionEvent event) {
        switchScene(event, SceneConfig.BIDDER_HISTORY);
    }
    @FXML
    protected void onActive(ActionEvent event) {
        switchScene(event, SceneConfig.BIDDER_ACTIVE);
    }
    @FXML
    protected void onAccount(ActionEvent event) {
        switchScene(event, SceneConfig.HOME);
    }
    @FXML
    protected void onLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        if(alert.showAndWait().get() == ButtonType.OK) {
            DataStorage.currentAccount = null;
            switchScene(event, SceneConfig.LOGIN);
        }
    }
}
