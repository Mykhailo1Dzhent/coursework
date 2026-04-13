package com.example.coursework.controllers;

import com.example.coursework.databases.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditNotificationController {

    @FXML private TextField fieldTitle;
    @FXML private TextArea fieldMessage;
    @FXML private Label errorLabel;

    private int notificationId;
    private Runnable onSuccess;

    public void setOnSuccess(Runnable callback) { this.onSuccess = callback; }

    public void setNotification(int id, String title, String message) {
        this.notificationId = id;
        fieldTitle.setText(title);
        fieldMessage.setText(message);
    }

    @FXML
    private void handleSave() {
        if (fieldTitle.getText().isBlank()) { errorLabel.setText("Title is required."); return; }
        if (fieldMessage.getText().isBlank()) { errorLabel.setText("Message is required."); return; }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE admin_notifications SET title = ?, message = ? WHERE id = ?")) {
            stmt.setString(1, fieldTitle.getText().trim());
            stmt.setString(2, fieldMessage.getText().trim());
            stmt.setInt(3, notificationId);
            stmt.executeUpdate();

            if (onSuccess != null) onSuccess.run();
            ((Stage) fieldTitle.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) fieldTitle.getScene().getWindow()).close();
    }
}