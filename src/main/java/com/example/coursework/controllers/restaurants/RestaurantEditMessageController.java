package com.example.coursework.controllers.restaurants;

import com.example.coursework.databases.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class RestaurantEditMessageController {

    @FXML private Label labelOrderId;
    @FXML private TextArea fieldMessage;
    @FXML private Label errorLabel;

    private int messageId;
    private int senderId;
    private Runnable onSuccess;

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void setMessage(int messageId, int orderId, String currentText, int senderId) {
        this.messageId = messageId;
        this.senderId = senderId;
        labelOrderId.setText(String.valueOf(orderId));
        fieldMessage.setText(currentText);
    }

    @FXML
    private void handleSave() {
        if (fieldMessage.getText().isBlank()) {
            errorLabel.setText("Message cannot be empty.");
            return;
        }

        String sql = "UPDATE messages SET message = ?, text = ? WHERE id = ? AND sender_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String trimmed = fieldMessage.getText().trim();
            stmt.setString(1, trimmed);
            stmt.setString(2, trimmed);
            stmt.setInt(3, messageId);
            stmt.setInt(4, senderId);
            int updated = stmt.executeUpdate();

            if (updated == 0) {
                errorLabel.setText("You can only edit your own messages.");
                return;
            }

            if (onSuccess != null) onSuccess.run();
            ((Stage) fieldMessage.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) fieldMessage.getScene().getWindow()).close();
    }
}
