package com.example.coursework.controllers;

import com.example.coursework.databases.DatabaseConnection;
import com.example.coursework.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;

public class RestaurantController {

    private User currentUser;
    private int restaurantId = -1;

    // ---- INFO ----
    @FXML private TextField infoName, infoCuisine, infoOpening, infoClosing;
    @FXML private Label infoMessage;

    // ---- MENU ITEMS ----
    @FXML private TableView<ObservableList<String>> dishesTable;
    @FXML private TableColumn<ObservableList<String>, String> colDishId, colDishName, colDishDescription, colDishPrice;

    // ---- ORDERS ----
    @FXML private TableView<ObservableList<String>> ordersTable;
    @FXML private TableColumn<ObservableList<String>, String> colOrderId, colOrderCustomer, colOrderDriver, colOrderStatus, colOrderTime, colOrderPrice;
    @FXML private ComboBox<String> filterOrderStatus;
    @FXML private TextField filterOrderCustomer;
    @FXML private DatePicker filterOrderDate;

    // ---- MESSAGES ----
    @FXML private TableView<ObservableList<String>> messagesTable;
    @FXML private TableColumn<ObservableList<String>, String> colMessageId, colMessageOrder, colMessageSender, colMessageText, colMessageTime;

    @FXML private TableView<ObservableList<String>> notificationsTable;
    @FXML private TableColumn<ObservableList<String>, String> colNotifId, colNotifTitle, colNotifMessage, colNotifTime, colNotifRead;


    // ---- STATISTICS ----
    @FXML private Label statTotalOrders, statCompletedOrders, statPendingOrders;
    @FXML private Label statRevenue, statAvgOrder, statMenuItems, statPopularDish;

    public void setUser(User user) {
        this.currentUser = user;
        loadRestaurantId();
        initializeData();
    }

    private void loadRestaurantId() {
        String sql = "SELECT id FROM restaurants WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                restaurantId = rs.getInt("id");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void initializeData() {
        filterOrderStatus.setItems(FXCollections.observableArrayList(
                "ALL", "PENDING", "ACCEPTED", "IN_DELIVERY", "COMPLETED", "CANCELLED"
        ));

        setupDishesTable();
        setupOrdersTable();
        setupMessagesTable();
        setupNotificationsTable();
        loadNotifications();
        loadInfo();
        loadDishes();
        loadOrders(null, null, null);
        loadMessages();
        loadStatistics();
    }

    // == INFO ==

    @FXML
    public void handleRefreshInfo() { loadInfo(); }

    private void loadInfo() {
        if (restaurantId == -1) {
            infoMessage.setTextFill(javafx.scene.paint.Color.RED);
            infoMessage.setText("No restaurant found. Please contact admin.");
            return;
        }
        String sql = "SELECT name, cuisine_type, opening_time, closing_time FROM restaurants WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                infoName.setText(rs.getString("name"));
                infoCuisine.setText(rs.getString("cuisine_type"));
                infoOpening.setText(rs.getString("opening_time"));
                infoClosing.setText(rs.getString("closing_time"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleSaveInfo() {
        if (restaurantId == -1) return;
        String sql = "UPDATE restaurants SET name = ?, cuisine_type = ?, opening_time = ?, closing_time = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, infoName.getText());
            stmt.setString(2, infoCuisine.getText());
            stmt.setString(3, infoOpening.getText());
            stmt.setString(4, infoClosing.getText());
            stmt.setInt(5, restaurantId);
            stmt.executeUpdate();
            infoMessage.setTextFill(javafx.scene.paint.Color.GREEN);
            infoMessage.setText("Saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            infoMessage.setTextFill(javafx.scene.paint.Color.RED);
            infoMessage.setText("Error saving changes.");
        }
    }

    // == MENU ITEMS ==

    private void setupDishesTable() {
        colDishId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colDishName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colDishDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colDishPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
    }

    private void loadDishes() {
        if (restaurantId == -1) return;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = "SELECT id, name, describtion, price FROM menu_items WHERE restaurant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= 4; i++) row.add(rs.getString(i) != null ? rs.getString(i) : "");
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        dishesTable.setItems(data);
    }

    @FXML private void handleRefreshDishes() { loadDishes(); }
    @FXML private void handleAddDish() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/coursework/add_dish_dialog.fxml"));
            Parent root = loader.load();

            AddDishController controller = loader.getController();
            controller.setRestaurantId(restaurantId);
            controller.setOnSuccess(this::loadDishes);

            Stage stage = new Stage();
            stage.setTitle("Add Dish");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private void handleEditDish() {
        ObservableList<String> selected = dishesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Edit Dish", "Please select a dish first."); return; }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/coursework/edit_dish_dialog.fxml"));
            Parent root = loader.load();

            EditDishController controller = loader.getController();
            controller.setDish(
                    Integer.parseInt(selected.get(0)), // id
                    selected.get(1),                   // name
                    selected.get(2),                   // description
                    selected.get(3)                    // price
            );
            controller.setOnSuccess(() -> loadDishes());

            Stage stage = new Stage();
            stage.setTitle("Edit Dish");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleDeleteDish() {
        ObservableList<String> selected = dishesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Delete Dish", "Please select a dish first."); return; }
        confirm("Delete dish " + selected.get(1) + "?", () -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM menu_items WHERE id = ?")) {
                stmt.setInt(1, Integer.parseInt(selected.get(0)));
                stmt.executeUpdate();
                loadDishes();
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    // == ORDERS ==

    private void setupOrdersTable() {
        colOrderId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colOrderCustomer.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colOrderDriver.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colOrderStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
        colOrderTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(4)));
        colOrderPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(5)));
    }

    private void loadOrders(String status, String customer, LocalDate date) {
        if (restaurantId == -1) return;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        StringBuilder sql = new StringBuilder(
                "SELECT o.id, cu.username, dr.username, o.status, o.order_time, o.total_price " +
                        "FROM orders o " +
                        "LEFT JOIN users cu ON o.customer_id = cu.id " +
                        "LEFT JOIN users dr ON o.driver_id = dr.id " +
                        "WHERE o.restaurant_id = ?"
        );
        if (status != null && !status.isEmpty() && !status.equals("ALL")) sql.append(" AND o.status = '").append(status).append("'");
        if (customer != null && !customer.isEmpty()) sql.append(" AND cu.username LIKE '%").append(customer).append("%'");
        if (date != null) sql.append(" AND DATE(o.order_time) = '").append(date).append("'");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= 6; i++) row.add(rs.getString(i) != null ? rs.getString(i) : "");
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        ordersTable.setItems(data);
    }

    @FXML private void handleFilterOrders() {
        loadOrders(filterOrderStatus.getValue(), filterOrderCustomer.getText(), filterOrderDate.getValue());
    }
    @FXML private void handleResetOrderFilter() {
        filterOrderStatus.setValue(null); filterOrderCustomer.clear(); filterOrderDate.setValue(null);
        loadOrders(null, null, null);
    }
    @FXML private void handleRefreshOrders() { loadOrders(null, null, null); }
    @FXML private void handleAcceptOrder() { showInfo("Accept Order", "Feature coming soon."); }
    @FXML private void handleChangeStatus() { showInfo("Change Status", "Feature coming soon."); }

    // == MESSAGES ==

    private void setupMessagesTable() {
        colMessageId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colMessageOrder.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colMessageSender.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colMessageText.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
        colMessageTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(4)));
    }

    private void loadMessages() {
        if (restaurantId == -1) return;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = "SELECT m.id, m.order_id, u.username, m.message, m.sent_at " +
                "FROM messages m " +
                "LEFT JOIN users u ON m.sender_id = u.id " +
                "LEFT JOIN orders o ON m.order_id = o.id " +
                "WHERE o.restaurant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= 5; i++) row.add(rs.getString(i) != null ? rs.getString(i) : "");
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        messagesTable.setItems(data);
    }

    @FXML private void handleNewMessage() { showInfo("Edit Message", "Feature coming soon."); }
    @FXML private void handleEditMessage() { showInfo("Edit Message", "Feature coming soon."); }
    @FXML private void handleDeleteMessage() {showInfo("Edit Message", "Feature coming soon."); }
    @FXML private void handleRefreshMessages() { loadMessages(); }
    private void setupNotificationsTable() {
        colNotifId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colNotifTitle.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colNotifMessage.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colNotifTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
        colNotifRead.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(4)));
    }

    private void loadNotifications() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = "SELECT id, title, message, sent_at, " +
                "CASE WHEN is_read THEN 'Yes' ELSE 'No' END " +
                "FROM admin_notifications " +
                "WHERE recipient_id = ? OR recipient_type = 'ALL_RESTAURANTS' " +
                "ORDER BY sent_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= 5; i++) row.add(rs.getString(i) != null ? rs.getString(i) : "");
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        notificationsTable.setItems(data);
    }

    @FXML private void handleRefreshNotifications() { loadNotifications(); }

    @FXML private void handleMarkAsRead() {
        ObservableList<String> selected = notificationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Mark as Read", "Please select a notification first."); return; }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE admin_notifications SET is_read = TRUE WHERE id = ?")) {
            stmt.setInt(1, Integer.parseInt(selected.get(0)));
            stmt.executeUpdate();
            loadNotifications();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // == STATISTICS ==

    private void loadStatistics() {
        if (restaurantId == -1) return;
        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement stmt;
            ResultSet rs;

            stmt = conn.prepareStatement("SELECT COUNT(*) FROM orders WHERE restaurant_id = ?");
            stmt.setInt(1, restaurantId);
            rs = stmt.executeQuery();
            if (rs.next()) statTotalOrders.setText(rs.getString(1));

            stmt = conn.prepareStatement("SELECT COUNT(*) FROM orders WHERE restaurant_id = ? AND status = 'COMPLETED'");
            stmt.setInt(1, restaurantId);
            rs = stmt.executeQuery();
            if (rs.next()) statCompletedOrders.setText(rs.getString(1));

            stmt = conn.prepareStatement("SELECT COUNT(*) FROM orders WHERE restaurant_id = ? AND status = 'PENDING'");
            stmt.setInt(1, restaurantId);
            rs = stmt.executeQuery();
            if (rs.next()) statPendingOrders.setText(rs.getString(1));

            stmt = conn.prepareStatement("SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE restaurant_id = ? AND status = 'COMPLETED'");
            stmt.setInt(1, restaurantId);
            rs = stmt.executeQuery();
            if (rs.next()) statRevenue.setText(rs.getString(1) + " €");

            stmt = conn.prepareStatement("SELECT COALESCE(AVG(total_price), 0) FROM orders WHERE restaurant_id = ?");
            stmt.setInt(1, restaurantId);
            rs = stmt.executeQuery();
            if (rs.next()) statAvgOrder.setText(String.format("%.2f €", rs.getDouble(1)));

            stmt = conn.prepareStatement("SELECT COUNT(*) FROM menu_items WHERE restaurant_id = ?");
            stmt.setInt(1, restaurantId);
            rs = stmt.executeQuery();
            if (rs.next()) statMenuItems.setText(rs.getString(1));

            stmt = conn.prepareStatement(
                    "SELECT m.name FROM order_items oi " +
                            "JOIN menu_items m ON oi.menu_item_id = m.id " +
                            "JOIN orders o ON oi.order_id = o.id " +
                            "WHERE o.restaurant_id = ? " +
                            "GROUP BY m.id ORDER BY SUM(oi.quantity) DESC LIMIT 1"
            );
            stmt.setInt(1, restaurantId);
            rs = stmt.executeQuery();
            statPopularDish.setText(rs.next() ? rs.getString(1) : "—");

        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleRefreshStatistics() { loadStatistics(); }

    // == HELPERS ==

    private void showInfo(String title, String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private void confirm(String message, Runnable action) {
        new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO)
                .showAndWait()
                .ifPresent(btn -> { if (btn == ButtonType.YES) action.run(); });
    }
}
