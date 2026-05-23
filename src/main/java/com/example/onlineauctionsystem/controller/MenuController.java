package com.example.onlineauctionsystem.controller;

import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public abstract class MenuController extends BaseController {

    @FXML
    protected void onMyProducts(ActionEvent event) {
        String role = DataStorage.currentAccount.getRole();
        if ("SELLER".equals(role)) {
            switchScene(event, SceneConfig.SELLER_PRODUCT);
        } else {
            switchScene(event, SceneConfig.BIDDER_PRODUCT);
        }
    }

    @FXML
    protected void onHistory(ActionEvent event) {
        if ("SELLER".equals(DataStorage.currentAccount.getRole())) {
            switchScene(event, SceneConfig.SELLER_HISTORY);
        } else {
            switchScene(event, SceneConfig.BIDDER_HISTORY);
        }
    }

    @FXML
    protected void onManage(ActionEvent event) {
        if ("SELLER".equals(DataStorage.currentAccount.getRole())) {
            switchScene(event, SceneConfig.SELLER_MANAGER);
        } else {
            switchScene(event, SceneConfig.BIDDER_MANAGER);
        }
    }

    @FXML
    protected void onAccount(ActionEvent event) {
        if ("SELLER".equals(DataStorage.currentAccount.getRole())) {
            switchScene(event, SceneConfig.SELLER_HOME);
        } else {
            switchScene(event, SceneConfig.BIDDER_HOME);
        }
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