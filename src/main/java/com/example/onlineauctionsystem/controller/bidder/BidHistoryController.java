package com.example.onlineauctionsystem.controller.bidder;

import com.example.onlineauctionsystem.controller.MenuController;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;

public class BidHistoryController extends MenuController {


    @FXML private VBox historyContainer;

    @FXML
    @Override
    public void onMyProducts(ActionEvent event) {
        stopAutoRefresh();
        super.onMyProducts(event);
    }

    @FXML
    @Override
    public void onHistory(ActionEvent event) {
        stopAutoRefresh();
        super.onHistory(event);
    }

    @FXML
    @Override
    public void onManage(ActionEvent event) {
        stopAutoRefresh();
        super.onManage(event);
    }

    @FXML
    @Override
    public void onAccount(ActionEvent event) {
        stopAutoRefresh();
        super.onAccount(event);
    }

    @Override
    @FXML
    public void onLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            stopAutoRefresh();
            DataStorage.currentAccount = null;
            switchScene(event, SceneConfig.LOGIN);
        }
    }
}
