package com.example.coursework.controllers.admins;

import com.example.coursework.databases.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class CreateRestaurantController {

    @FXML private ComboBox<String> fieldOwner;
    @FXML private TextField fieldName, fieldCuisine, fieldOpening, fieldClosing;
    @FXML private Label errorLabel;

    private Runnable onSuccess;
    private final Map<String, Integer> ownerMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadOwners();
    }

    private void loadOwners() {
        String sql = "SELECT u.id, u.username FROM users u " +
                "LEFT JOIN restaurants r ON u.id = r.user_id " +
                "WHERE u.role = 'RESTAURANT' AND r.id IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                ownerMap.put(username, id);
                fieldOwner.getItems().add(username);
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (fieldOwner.getItems().isEmpty()) {
            fieldOwner.setPromptText("No available owners");
        }
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    private void handleSave() {
        if (fieldOwner.getValue() == null) {
            errorLabel.setText("Please select an owner.");
            return;
        }
        if (fieldName.getText().isBlank()) {
            errorLabel.setText("Restaurant name is required.");
            return;
        }
        if (fieldCuisine.getText().isBlank()) {
            errorLabel.setText("Cuisine type is required.");
            return;
        }
        if (fieldOpening.getText().isBlank() || fieldClosing.getText().isBlank()) {
            errorLabel.setText("Opening and closing time are required.");
            return;
        }

        int ownerId = ownerMap.get(fieldOwner.getValue());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO restaurants (user_id, name, cuisine_type, opening_time, closing_time) VALUES (?, ?, ?, ?, ?)"
             )) {
            stmt.setInt(1, ownerId);
            stmt.setString(2, fieldName.getText().trim());
            stmt.setString(3, fieldCuisine.getText().trim());
            stmt.setString(4, fieldOpening.getText().trim());
            stmt.setString(5, fieldClosing.getText().trim());
            stmt.executeUpdate();

            if (onSuccess != null) onSuccess.run();
            ((Stage) fieldName.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) fieldName.getScene().getWindow()).close();
    }
}