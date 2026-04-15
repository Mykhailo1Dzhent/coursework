package com.example.coursework.controllers.general;

import com.example.coursework.databases.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddDishController {

    @FXML private TextField fieldName, fieldPrice;
    @FXML private TextArea fieldDescription;
    @FXML private Label errorLabel;

    private int restaurantId;
    private Runnable onSuccess;

    public void setRestaurantId(int id) {
        this.restaurantId = id;
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    private void handleSave() {
        if (fieldName.getText().isBlank()) {
            errorLabel.setText("Name is required.");
            return;
        }
        if (fieldPrice.getText().isBlank()) {
            errorLabel.setText("Price is required.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(fieldPrice.getText().trim());
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorLabel.setText("Price must be a positive number.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO menu_items (restaurant_id, name, describtion, price) VALUES (?, ?, ?, ?)"
             )) {
            stmt.setInt(1, restaurantId);
            stmt.setString(2, fieldName.getText().trim());
            stmt.setString(3, fieldDescription.getText().trim());
            stmt.setDouble(4, price);
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