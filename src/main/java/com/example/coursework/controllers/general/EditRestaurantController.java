package com.example.coursework.controllers.general;

import com.example.coursework.databases.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class EditRestaurantController {

    @FXML private ComboBox<String> fieldOwner;
    @FXML private TextField fieldName, fieldCuisine, fieldOpening, fieldClosing;
    @FXML private Label errorLabel;

    private int restaurantId;
    private Runnable onSuccess;

    // username -> user_id
    private Map<String, Integer> ownerMap = new HashMap<>();

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void setRestaurant(int id, String name, String cuisine, String opening, String closing) {
        this.restaurantId = id;
        fieldName.setText(name);
        fieldCuisine.setText(cuisine);
        fieldOpening.setText(opening);
        fieldClosing.setText(closing);
        loadOwners();
    }

    // Отдельный метод чтобы выбрать текущего владельца после загрузки
    public void setCurrentOwner(String ownerUsername) {
        fieldOwner.setValue(ownerUsername);
    }

    private void loadOwners() {
        String sql = "SELECT id, username FROM users WHERE role = 'RESTAURANT'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ownerMap.put(rs.getString("username"), rs.getInt("id"));
            }
            fieldOwner.setItems(FXCollections.observableArrayList(ownerMap.keySet()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        if (fieldName.getText().isBlank()) {
            errorLabel.setText("Name is required.");
            return;
        }
        if (fieldOwner.getValue() == null) {
            errorLabel.setText("Owner is required.");
            return;
        }
        if (fieldOpening.getText().isBlank() || fieldClosing.getText().isBlank()) {
            errorLabel.setText("Opening and closing time are required.");
            return;
        }

        int ownerId = ownerMap.get(fieldOwner.getValue());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE restaurants SET name = ?, cuisine_type = ?, opening_time = ?, closing_time = ?, user_id = ? WHERE id = ?"
             )) {
            stmt.setString(1, fieldName.getText().trim());
            stmt.setString(2, fieldCuisine.getText().trim());
            stmt.setString(3, fieldOpening.getText().trim());
            stmt.setString(4, fieldClosing.getText().trim());
            stmt.setInt(5, ownerId);
            stmt.setInt(6, restaurantId);
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