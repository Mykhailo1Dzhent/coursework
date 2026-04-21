package com.example.coursework.controllers.restaurants;

import com.example.coursework.databases.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class RestaurantNewMessageController {

    @FXML private ComboBox<String> orderCombo;
    @FXML private TextArea fieldMessage;
    @FXML private Label errorLabel;

    private int restaurantId;
    private int senderId;
    private Runnable onSuccess;

    private final Map<String, Integer> orderMap = new LinkedHashMap<>();

    public void setContext(int restaurantId, int senderId) {
        this.restaurantId = restaurantId;
        this.senderId = senderId;
        loadOrders();
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    private void loadOrders() {
        orderMap.clear();
        String sql = "SELECT id, status, order_time FROM orders " +
                "WHERE restaurant_id = ? ORDER BY order_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String label = "Order #" + id + " — " + rs.getString("status")
                        + " (" + rs.getString("order_time") + ")";
                orderMap.put(label, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        orderCombo.setItems(FXCollections.observableArrayList(orderMap.keySet()));
    }

    @FXML
    private void handleSave() {
        String selectedLabel = orderCombo.getValue();
        if (selectedLabel == null) {
            errorLabel.setText("Please select an order.");
            return;
        }
        if (fieldMessage.getText().isBlank()) {
            errorLabel.setText("Message cannot be empty.");
            return;
        }

        int orderId = orderMap.get(selectedLabel);

        String sql = "INSERT INTO messages (order_id, sender_id, message, text, sent_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, senderId);
            String body = fieldMessage.getText().trim();
            stmt.setString(3, body);
            stmt.setString(4, body);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();

            if (onSuccess != null) onSuccess.run();
            ((Stage) fieldMessage.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) orderCombo.getScene().getWindow()).close();
    }
}
