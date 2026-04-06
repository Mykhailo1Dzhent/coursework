package com.example.coursework.controllers;

import com.example.coursework.databases.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditUserController {

    @FXML private TextField fieldUsername, fieldName, fieldSurname, fieldEmail;
    @FXML private ComboBox<String> fieldRole;
    @FXML private Label errorLabel;

    private int userId;
    private String originalRole;
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

    // Вызывается из AdminController — заполняет поля данными выбранного пользователя
    public void setUser(int id, String username, String role, String name, String surname, String email) {
        this.userId = id;
        this.originalRole = role;
        fieldUsername.setText(username);
        fieldRole.setValue(role);
        fieldName.setText(name);
        fieldSurname.setText(surname);
        fieldEmail.setText(email);
    }

    @FXML
    private void handleSave() {
        if (fieldUsername.getText().isBlank()) {
            errorLabel.setText("Username is required.");
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

        String newRole = fieldRole.getValue();
        String name = fieldName.getText().trim();
        String surname = fieldSurname.getText().trim();
        String email = fieldEmail.getText().trim();

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Обновляем users
            PreparedStatement stmtUser = conn.prepareStatement(
                    "UPDATE users SET username = ?, role = ? WHERE id = ?"
            );
            stmtUser.setString(1, fieldUsername.getText().trim());
            stmtUser.setString(2, newRole);
            stmtUser.setInt(3, userId);
            stmtUser.executeUpdate();

            // Если роль изменилась — удаляем старую запись из подтаблицы
            if (!newRole.equals(originalRole)) {
                String oldTable = getRoleTable(originalRole);
                if (oldTable != null) {
                    conn.prepareStatement("DELETE FROM " + oldTable + " WHERE user_id = " + userId).executeUpdate();
                }
                // Создаём новую запись в новой подтаблице
                String newTable = getRoleTable(newRole);
                if (newTable != null) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO " + newTable + " (user_id, name, surname, email) VALUES (?, ?, ?, ?)"
                    );
                    stmt.setInt(1, userId);
                    stmt.setString(2, name);
                    stmt.setString(3, surname);
                    stmt.setString(4, email);
                    stmt.executeUpdate();
                }
            } else {
                // Роль не изменилась — просто обновляем данные в подтаблице
                String table = getRoleTable(newRole);
                if (table != null) {
                    // Проверяем есть ли уже запись
                    ResultSet rs = conn.prepareStatement(
                            "SELECT id FROM " + table + " WHERE user_id = " + userId
                    ).executeQuery();

                    if (rs.next()) {
                        PreparedStatement stmt = conn.prepareStatement(
                                "UPDATE " + table + " SET name = ?, surname = ?, email = ? WHERE user_id = ?"
                        );
                        stmt.setString(1, name);
                        stmt.setString(2, surname);
                        stmt.setString(3, email);
                        stmt.setInt(4, userId);
                        stmt.executeUpdate();
                    } else {
                        PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO " + table + " (user_id, name, surname, email) VALUES (?, ?, ?, ?)"
                        );
                        stmt.setInt(1, userId);
                        stmt.setString(2, name);
                        stmt.setString(3, surname);
                        stmt.setString(4, email);
                        stmt.executeUpdate();
                    }
                }
            }

            if (onSuccess != null) onSuccess.run();
            ((Stage) fieldUsername.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getRoleTable(String role) {
        return switch (role) {
            case "CUSTOMER" -> "customers";
            case "DRIVER" -> "drivers";
            case "RESTAURANT" -> "restaurant_owners";
            case "ADMIN" -> "admins";
            default -> null;
        };
    }

    @FXML
    private void handleCancel() {
        ((Stage) fieldUsername.getScene().getWindow()).close();
    }
}