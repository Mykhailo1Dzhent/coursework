package com.example.coursework.controllers.restaurants;

import com.example.coursework.databases.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RestaurantOrderItemsController {

    @FXML private Label labelOrder;
    @FXML private Label hintLabel;
    @FXML private TableView<ObservableList<String>> linesTable;
    @FXML private TableColumn<ObservableList<String>, String> colLineId;
    @FXML private TableColumn<ObservableList<String>, String> colLineName;
    @FXML private TableColumn<ObservableList<String>, String> colLineQty;
    @FXML private TableColumn<ObservableList<String>, String> colLineUnit;
    @FXML private TableColumn<ObservableList<String>, String> colLineTotal;

    private int orderId;
    private int restaurantId;
    private Runnable onSuccess;

    public void setContext(int orderId, int restaurantId, Runnable onSuccess) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.onSuccess = onSuccess;
        labelOrder.setText("Order #" + orderId);
        hintLabel.setText("");
        setupTable();
        loadData();
    }

    private void setupTable() {
        colLineId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colLineName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colLineQty.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colLineUnit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colLineTotal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
    }

    private void loadData() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = "SELECT oi.id, m.name, oi.quantity, oi.unit_price, (oi.quantity * oi.unit_price) AS line_total " +
                "FROM order_items oi JOIN menu_items m ON oi.menu_item_id = m.id " +
                "WHERE oi.order_id = ? ORDER BY oi.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("quantity"));
                row.add(String.format("%.2f", rs.getDouble("unit_price")));
                row.add(String.format("%.2f", rs.getDouble("line_total")));
                data.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            hintLabel.setText("Error loading lines: " + e.getMessage());
        }
        linesTable.setItems(data);
    }

    @FXML
    private void handleRemoveSelected() {
        hintLabel.setText("");
        ObservableList<String> selected = linesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            hintLabel.setText("Select a line to remove.");
            return;
        }
        int lineId = Integer.parseInt(selected.get(0));
        new Alert(Alert.AlertType.CONFIRMATION, "Remove this line from the order and recalculate total price?")
                .showAndWait()
                .ifPresent(btn -> {
                    if (btn != ButtonType.OK) return;
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        conn.setAutoCommit(false);
                        try {
                            try (PreparedStatement chk = conn.prepareStatement(
                                    "SELECT id FROM orders WHERE id = ? AND restaurant_id = ?")) {
                                chk.setInt(1, orderId);
                                chk.setInt(2, restaurantId);
                                if (!chk.executeQuery().next()) {
                                    conn.rollback();
                                    hintLabel.setText("Order not found for this restaurant.");
                                    return;
                                }
                            }
                            int deleted;
                            try (PreparedStatement del = conn.prepareStatement(
                                    "DELETE FROM order_items WHERE id = ? AND order_id = ?")) {
                                del.setInt(1, lineId);
                                del.setInt(2, orderId);
                                deleted = del.executeUpdate();
                            }
                            if (deleted == 0) {
                                conn.rollback();
                                hintLabel.setText("Line could not be removed.");
                                return;
                            }
                            double newTotal = sumOrderItems(conn, orderId);
                            try (PreparedStatement up = conn.prepareStatement(
                                    "UPDATE orders SET total_price = ? WHERE id = ? AND restaurant_id = ?")) {
                                up.setDouble(1, newTotal);
                                up.setInt(2, orderId);
                                up.setInt(3, restaurantId);
                                up.executeUpdate();
                            }
                            conn.commit();
                            if (onSuccess != null) onSuccess.run();
                            loadData();
                        } catch (Exception e) {
                            conn.rollback();
                            throw e;
                        } finally {
                            conn.setAutoCommit(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        hintLabel.setText("Error: " + e.getMessage());
                    }
                });
    }

    private static double sumOrderItems(Connection conn, int orderId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(unit_price * quantity), 0) FROM order_items WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Math.round(rs.getDouble(1) * 100.0) / 100.0;
            }
        }
        return 0;
    }

    @FXML
    private void handleClose() {
        Stage s = (Stage) linesTable.getScene().getWindow();
        s.close();
    }
}
