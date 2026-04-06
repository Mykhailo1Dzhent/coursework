package com.example.coursework.controllers;

import com.example.coursework.databases.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditDishController {

    @FXML private TextField fieldName, fieldPrice;
    @FXML private TextArea fieldDescription;
    @FXML private Label errorLabel;

    private int dishId;
    private Runnable onSuccess;

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    // Вызывается из AdminController — заполняет поля данными выбранного блюда
    public void setDish(int id, String name, String description, String price) {
        this.dishId = id;
        fieldName.setText(name);
        fieldDescription.setText(description);
        fieldPrice.setText(price);
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
                     "UPDATE menu_items SET name = ?, describtion = ?, price = ? WHERE id = ?"
             )) {
            stmt.setString(1, fieldName.getText().trim());
            stmt.setString(2, fieldDescription.getText().trim());
            stmt.setDouble(3, price);
            stmt.setInt(4, dishId);
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