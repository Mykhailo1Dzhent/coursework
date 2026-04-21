package com.example.coursework.controllers.admins;

import com.example.coursework.databases.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewMessageController {

    @FXML private TextField fieldTitle, fieldSearch;
    @FXML private TextArea fieldMessage;
    @FXML private ComboBox<String> fieldGroup;
    @FXML private ListView<String> searchResults;
    @FXML private Label errorLabel, recipientLabel;

    private Runnable onSuccess;
    private Integer selectedUserId = null; // null = группа

    private Map<String, Integer> searchMap = new HashMap<>();

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    public void initialize() {
        fieldGroup.setItems(FXCollections.observableArrayList(
                "All Restaurant Owners"
        ));

        fieldGroup.setOnAction(e -> {
            selectedUserId = null;
            searchResults.getItems().clear();
            fieldSearch.clear();
            if (fieldGroup.getValue() != null) {
                recipientLabel.setText("Recipient: " + fieldGroup.getValue());
            }
        });

        fieldSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isBlank()) {
                searchResults.getItems().clear();
                searchMap.clear();
                return;
            }
            searchUsers(newVal.trim());
        });

        searchResults.setOnMouseClicked(e -> {
            String selected = searchResults.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedUserId = searchMap.get(selected);
                fieldGroup.setValue(null);
                recipientLabel.setText("Recipient: " + selected);
            }
        });
    }

    private void searchUsers(String query) {
        searchMap.clear();
        List<String> results = new ArrayList<>();
        String sql = "SELECT id, username FROM users WHERE role = 'RESTAURANT' AND username LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                searchMap.put(username, rs.getInt("id"));
                results.add(username);
            }
        } catch (Exception e) { e.printStackTrace(); }
        searchResults.setItems(FXCollections.observableArrayList(results));
    }

    @FXML
    private void handleSave() {
        if (fieldTitle.getText().isBlank()) { errorLabel.setText("Title is required."); return; }
        if (fieldMessage.getText().isBlank()) { errorLabel.setText("Message is required."); return; }
        if (selectedUserId == null && fieldGroup.getValue() == null) {
            errorLabel.setText("Please select a recipient."); return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (selectedUserId != null) {
                String sql = "INSERT INTO admin_notifications (recipient_id, recipient_type, title, message) VALUES (?, NULL, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, selectedUserId);
                    stmt.setString(2, fieldTitle.getText().trim());
                    stmt.setString(3, fieldMessage.getText().trim());
                    stmt.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO admin_notifications (recipient_id, recipient_type, title, message) VALUES (NULL, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, "ALL_RESTAURANTS");
                    stmt.setString(2, fieldTitle.getText().trim());
                    stmt.setString(3, fieldMessage.getText().trim());
                    stmt.executeUpdate();
                }
            }

            if (onSuccess != null) onSuccess.run();
            ((Stage) fieldTitle.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendMessage(Connection conn, int recipientId, String title, String text) throws SQLException {
        String sql = "INSERT INTO admin_notifications (recipient_id, title, message) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recipientId);
            stmt.setString(2, title);
            stmt.setString(3, text);
            stmt.executeUpdate();
        }
    }

    private List<Integer> getUserIdsByRole(Connection conn, String role) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id FROM users WHERE role = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) ids.add(rs.getInt("id"));
        }
        return ids;
    }

    @FXML
    private void handleCancel() {
        ((Stage) fieldTitle.getScene().getWindow()).close();
    }
}