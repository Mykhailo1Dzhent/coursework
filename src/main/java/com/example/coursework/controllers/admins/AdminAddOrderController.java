package com.example.coursework.controllers.admins;

import com.example.coursework.databases.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class AdminAddOrderController {

    @FXML private ComboBox<String> comboCustomer;
    @FXML private ComboBox<String> comboRestaurant;
    @FXML private ComboBox<String> comboDriver;
    @FXML private ComboBox<String> comboStatus;
    @FXML private TextField fieldTotalPrice;
    @FXML private Label errorLabel;

    private final Map<String, Integer> customerIds = new HashMap<>();
    private final Map<String, Integer> restaurantIds = new HashMap<>();
    private final Map<String, Integer> driverIds = new HashMap<>();
    private static final String NO_DRIVER = "(none)";

    private Runnable onSuccess;

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    public void initialize() {
        comboStatus.setItems(FXCollections.observableArrayList(
                "PENDING", "ACCEPTED", "READY", "IN_DELIVERY", "COMPLETED", "CANCELLED"
        ));
        comboStatus.getSelectionModel().selectFirst();
        loadCombos();
    }

    private void loadCombos() {
        errorLabel.setText("");
        customerIds.clear();
        restaurantIds.clear();
        driverIds.clear();
        comboCustomer.getItems().clear();
        comboRestaurant.getItems().clear();
        comboDriver.getItems().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT id, username FROM users WHERE role = 'CUSTOMER' ORDER BY username")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String label = rs.getString("username") + " (" + id + ")";
                    customerIds.put(label, id);
                    comboCustomer.getItems().add(label);
                }
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, name FROM restaurants ORDER BY name")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String label = name + " (" + id + ")";
                    restaurantIds.put(label, id);
                    comboRestaurant.getItems().add(label);
                }
            }
            comboDriver.getItems().add(NO_DRIVER);
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT id, username FROM users WHERE role = 'DRIVER' ORDER BY username")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String label = rs.getString("username") + " (" + id + ")";
                    driverIds.put(label, id);
                    comboDriver.getItems().add(label);
                }
            }
            comboDriver.getSelectionModel().selectFirst();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Could not load lists: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");
        String custLabel = comboCustomer.getValue();
        String restLabel = comboRestaurant.getValue();
        String status = comboStatus.getValue();
        if (custLabel == null || !customerIds.containsKey(custLabel)) {
            errorLabel.setText("Select a customer.");
            return;
        }
        if (restLabel == null || !restaurantIds.containsKey(restLabel)) {
            errorLabel.setText("Select a restaurant.");
            return;
        }
        if (status == null || status.isBlank()) {
            errorLabel.setText("Select a status.");
            return;
        }
        double total;
        try {
            String raw = fieldTotalPrice.getText() == null ? "" : fieldTotalPrice.getText().trim().replace(',', '.');
            if (raw.isEmpty()) {
                errorLabel.setText("Enter total price.");
                return;
            }
            total = Double.parseDouble(raw);
            if (total < 0) {
                errorLabel.setText("Price cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid total price.");
            return;
        }

        int customerId = customerIds.get(custLabel);
        int restaurantId = restaurantIds.get(restLabel);
        String drvLabel = comboDriver.getValue();
        Integer driverId = (drvLabel == null || NO_DRIVER.equals(drvLabel)) ? null : driverIds.get(drvLabel);
        if (drvLabel != null && !NO_DRIVER.equals(drvLabel) && driverId == null) {
            errorLabel.setText("Select a valid driver or (none).");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO orders (customer_id, restaurant_id, driver_id, status, order_time, total_price) "
                             + "VALUES (?, ?, ?, ?, NOW(), ?)")) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, restaurantId);
            if (driverId == null) {
                stmt.setNull(3, Types.INTEGER);
            } else {
                stmt.setInt(3, driverId);
            }
            stmt.setString(4, status);
            stmt.setDouble(5, Math.round(total * 100.0) / 100.0);
            stmt.executeUpdate();
            if (onSuccess != null) onSuccess.run();
            ((Stage) fieldTotalPrice.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) fieldTotalPrice.getScene().getWindow()).close();
    }
}
