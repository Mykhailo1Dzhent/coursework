package com.example.coursework.controllers;

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

    /**
     * @param messageId  ID записи в таблице messages
     * @param orderId    для отображения
     * @param currentText текущий текст сообщения
     * @param senderId   ID текущего пользователя (для проверки авторства)
     */
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

        // Обновляем только если это сообщение принадлежит текущему пользователю
        String sql = "UPDATE messages SET message = ? WHERE id = ? AND sender_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fieldMessage.getText().trim());
            stmt.setInt(2, messageId);
            stmt.setInt(3, senderId);
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
