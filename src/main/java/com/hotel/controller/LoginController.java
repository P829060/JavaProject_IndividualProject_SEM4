package com.hotel.controller;

import com.hotel.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if ("admin".equals(username) && "admin123".equals(password)) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(ViewLoader.load("dashboard-view.fxml"), 1200, 760);
            scene.getStylesheets().add(ViewLoader.resource("css/app.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Hotel Dashboard");
        } else {
            messageLabel.setText("Invalid Login details, Please enter again");
        }
    }
}
