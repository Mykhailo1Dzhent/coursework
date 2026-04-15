package com.example.coursework.controllers;

import com.example.coursework.controllers.restaurants.RestaurantController;
import com.example.coursework.models.User;
import com.example.coursework.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Enter your login and password");
            return;
        }

        User user = userService.login(username, password);

        if (user == null) {
            messageLabel.setText("Incorrect login or password");
            return;
        }

        if (user.getRole().equals("CUSTOMER") || user.getRole().equals("DRIVER")) {
            messageLabel.setText("Please use the web app to login");
            return;
        }

        openRoleWindow(user);
    }

    private void openRoleWindow(User user) {
        String fxmlFile = "";

        switch (user.getRole()) {
            case "ADMIN":
                fxmlFile = "/com/example/coursework/admin-view.fxml";
                break;
            case "RESTAURANT":
                fxmlFile = "/com/example/coursework/restaurant-view.fxml";
                break;
            default:
                messageLabel.setText("Unknown role");
                return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), 1000, 700);

            if (user.getRole().equals("RESTAURANT")) {
                RestaurantController controller = loader.getController();
                controller.setUser(user);
            }

            Stage stage = new Stage();
            stage.setTitle(user.getRole() + " panel");
            stage.setScene(scene);
            stage.show();

            usernameField.getScene().getWindow().hide();

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Opening window error");
        }
    }
}
