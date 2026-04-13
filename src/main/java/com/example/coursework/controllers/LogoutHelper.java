package com.example.coursework.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LogoutHelper {

    public static void logout(Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    LogoutHelper.class.getResource("/com/example/coursework/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(scene);
            loginStage.show();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}