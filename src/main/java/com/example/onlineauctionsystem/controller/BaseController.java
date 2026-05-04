package com.example.onlineauctionsystem.controller;

import com.example.onlineauctionsystem.utils.SceneConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;


public abstract class BaseController{
    protected void switchScene(ActionEvent event, String fxmlPath, String title){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void switchScene(ActionEvent event, SceneConfig sceneType){
        switchScene(event, sceneType.getPath(), sceneType.getTitle());
    }
    protected void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void setUpLabel(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    protected void updateLabel(Label label, String text, String color) {
        label.setText(text);
        label.setStyle("-fx-text-fill: " + color + ";");
        label.setVisible(true);
        label.setManaged(true);
    }
}