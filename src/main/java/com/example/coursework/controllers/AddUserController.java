package com.example.coursework.controllers;

import com.example.coursework.databases.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddUserController {

    @FXML private TextField fieldUsername, fieldName, fieldSurname, fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private ComboBox<String> fieldRole;
    @FXML private Label errorLabel;

    private Runnable onSuccess;

    @FXML
    public void initialize() {
        fieldRole.setItems(FXCollections.observableArrayList(
                "CUSTOMER", "DRIVER", "RESTAURANT", "ADMIN"
        ));
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    private void handleSave() {
        if (fieldUsername.getText().isBlank()) {
            errorLabel.setText("Username is required.");
            return;
        }
        if (fieldPassword.getText().isBlank()) {
            errorLabel.setText("Password is required.");
            return;
        }
        if (fieldRole.getValue() == null) {
            errorLabel.setText("Please select a role.");
            return;
        }
        if (fieldEmail.getText().isBlank()) {
            errorLabel.setText("Email is required.");
            return;
        }

        String role = fieldRole.getValue();

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmtUser = conn.prepareStatement(
                    "insert into users (username, password, role) values (?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
            );
            stmtUser.setString(1, fieldUsername.getText().trim());
            stmtUser.setString(2, BCrypt.hashpw(fieldPassword.getText(), BCrypt.gensalt()));
            stmtUser.setString(3, role);
            stmtUser.executeUpdate();

            var keys = stmtUser.getGeneratedKeys();
            if (!keys.next()) throw new Exception("Failed to get user id");
            int userId = keys.getInt(1);

            String name = fieldName.getText().trim();
            String surname = fieldSurname.getText().trim();
            String email = fieldEmail.getText().trim();

            String roleTable = switch (role) {
                case "CUSTOMER" -> "customers";
                case "DRIVER" -> "drivers";
                case "RESTAURANT" -> "restaurant_owners";
                case "ADMIN" -> "admins";
                default -> null;
            };

            if (roleTable != null) {
                PreparedStatement stmtRole = conn.prepareStatement(
                        "insert into " + roleTable + " (user_id, name, surname, email) values (?, ?, ?, ?)"
                );
                stmtRole.setInt(1, userId);
                stmtRole.setString(2, name);
                stmtRole.setString(3, surname);
                stmtRole.setString(4, email);
                stmtRole.executeUpdate();
            }

            if (onSuccess != null) onSuccess.run();
            ((Stage) fieldUsername.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) fieldUsername.getScene().getWindow()).close();
    }
}