package com.example.coursework.controllers;

import com.example.coursework.databases.DatabaseConnection;
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
import javafx.scene.Parent;

public class AdminController {

    @FXML private TabPane tabPane;

    // ---- USERS ----
    @FXML private TableView<ObservableList<String>> usersTable;
    @FXML private TableColumn<ObservableList<String>, String> colUserId, colUsername, colRole, colName, colSurname, colEmail;
    @FXML private TextField filterUsername, filterName, filterSurname, filterEmail;
    @FXML private ComboBox<String> filterRole;

    // ---- RESTAURANTS ----
    @FXML private TableView<ObservableList<String>> restaurantsTable;
    @FXML private TableColumn<ObservableList<String>, String> colRestId, colRestName, colRestCuisine, colRestOwner, colRestOpening, colRestClosing;
    @FXML private TableView<ObservableList<String>> dishesTable;
    @FXML private TableColumn<ObservableList<String>, String> colDishId, colDishName, colDishDescription, colDishPrice;
    @FXML private Label dishesLabel;

    // ---- ORDERS ----
    @FXML private TableView<ObservableList<String>> ordersTable;
    @FXML private TableColumn<ObservableList<String>, String> colOrderId, colOrderCustomer, colOrderRestaurant, colOrderDriver, colOrderStatus, colOrderTime, colOrderPrice;
    @FXML private ComboBox<String> filterOrderStatus;
    @FXML private TextField filterOrderRestaurant, filterOrderCustomer;
    @FXML private DatePicker filterOrderDate;

    // ---- MESSAGES ----
    @FXML private TableView<ObservableList<String>> messagesTable;
    @FXML private TableColumn<ObservableList<String>, String> colMessageId, colMessageOrder, colMessageSender, colMessageText, colMessageTime;

    // ---- SUPPORT ----
    @FXML private TableView<ObservableList<String>> supportTable;
    @FXML private TableColumn<ObservableList<String>, String> colSupportId, colSupportUser, colSupportMessage, colSupportTime, colSupportStatus;

    // ---- STATISTICS ----
    @FXML private Label statTotalUsers, statCustomers, statDrivers, statRestaurantOwners;
    @FXML private Label statRestaurants, statTotalOrders, statCompletedOrders, statPendingOrders;

    @FXML
    public void initialize() {
        filterRole.setItems(FXCollections.observableArrayList("ALL", "ADMIN", "RESTAURANT", "CUSTOMER", "DRIVER"));
        filterOrderStatus.setItems(FXCollections.observableArrayList("ALL", "PENDING", "ACCEPTED", "IN_DELIVERY", "COMPLETED", "CANCELLED"));

        setupUsersTable();
        setupRestaurantsTable();
        setupDishesTable();
        setupOrdersTable();
        setupMessagesTable();
        setupSupportTable();

        loadUsers(null, null, null, null, null);
        loadRestaurants();
        loadOrders(null, null, null, null);
        loadMessages();
        loadStatistics();

        restaurantsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String restId = newVal.get(0);
                String restName = newVal.get(1);
                dishesLabel.setText("Dishes — " + restName);
                loadDishes(restId);
            }
        });
    }

    // == USERS ==

    private void setupUsersTable() {
        colUserId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colUsername.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colRole.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
        colSurname.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(4)));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(5)));
    }

    private void loadUsers(String username, String name, String surname, String role, String email) {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder(
                "SELECT u.id, u.username, u.role, " +
                        "COALESCE(c.name, d.name, ro.name, a.name, '') as name, " +
                        "COALESCE(c.surname, d.surname, ro.surname, a.surname, '') as surname, " +
                        "COALESCE(c.email, d.email, ro.email, a.email, '') as email " +
                        "FROM users u " +
                        "LEFT JOIN customers c ON u.id = c.user_id " +
                        "LEFT JOIN drivers d ON u.id = d.user_id " +
                        "LEFT JOIN restaurant_owners ro ON u.id = ro.user_id " +
                        "LEFT JOIN admins a ON u.id = a.user_id WHERE 1=1"
        );

        if (username != null && !username.isEmpty()) sql.append(" AND u.username LIKE '%").append(username).append("%'");
        if (name != null && !name.isEmpty()) sql.append(" AND (c.name LIKE '%").append(name).append("%' OR d.name LIKE '%").append(name).append("%' OR ro.name LIKE '%").append(name).append("%' OR a.name LIKE '%").append(name).append("%')");
        if (surname != null && !surname.isEmpty()) sql.append(" AND (c.surname LIKE '%").append(surname).append("%' OR d.surname LIKE '%").append(surname).append("%' OR ro.surname LIKE '%").append(surname).append("%' OR a.surname LIKE '%").append(surname).append("%')");
        if (role != null && !role.isEmpty() && !role.equals("ALL")) sql.append(" AND u.role = '").append(role).append("'");
        if (email != null && !email.isEmpty()) sql.append(" AND (c.email LIKE '%").append(email).append("%' OR d.email LIKE '%").append(email).append("%' OR ro.email LIKE '%").append(email).append("%' OR a.email LIKE '%").append(email).append("%')");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("id"));
                row.add(rs.getString("username"));
                row.add(rs.getString("role"));
                row.add(rs.getString("name"));
                row.add(rs.getString("surname"));
                row.add(rs.getString("email"));
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }

        usersTable.setItems(data);
    }

    @FXML private void handleFilterUsers() {
        loadUsers(filterUsername.getText(), filterName.getText(), filterSurname.getText(), filterRole.getValue(), filterEmail.getText());
    }

    @FXML private void handleResetUserFilter() {
        filterUsername.clear(); filterName.clear(); filterSurname.clear(); filterEmail.clear(); filterRole.setValue(null);
        loadUsers(null, null, null, null, null);
    }

    @FXML private void handleRefreshUsers() { loadUsers(null, null, null, null, null); }

    @FXML private void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/coursework/add_user_dialog.fxml"));
            Parent root = loader.load();

            AddUserController controller = loader.getController();
            controller.setOnSuccess(() -> loadUsers(null, null, null, null, null));

            Stage stage = new Stage();
            stage.setTitle("Add User");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleEditUser() {
        ObservableList<String> selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Edit User", "Please select a user first."); return; }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/coursework/edit_user_dialog.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();
            controller.setUser(
                    Integer.parseInt(selected.get(0)), // id
                    selected.get(1),                   // username
                    selected.get(2),                   // role
                    selected.get(3),                   // name
                    selected.get(4),                   // surname
                    selected.get(5)                    // email
            );
            controller.setOnSuccess(() -> loadUsers(null, null, null, null, null));

            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleDeleteUser() {
        ObservableList<String> selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Delete User", "Please select a user first."); return; }
        confirm("Delete user " + selected.get(1) + "?", () -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                int userId = Integer.parseInt(selected.get(0));
                for (String table : new String[]{"customers", "drivers", "restaurant_owners", "admins"}) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "DELETE FROM " + table + " WHERE user_id = ?")) {
                        stmt.setInt(1, userId);
                        stmt.executeUpdate();
                    }
                }
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM users WHERE id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                loadUsers(null, null, null, null, null);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    // == RESTAURANTS ==

    private void setupRestaurantsTable() {
        colRestId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colRestName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colRestCuisine.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colRestOwner.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
        colRestOpening.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(4)));
        colRestClosing.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(5)));
    }

    private void loadRestaurants() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = "SELECT r.id, r.name, r.cuisine_type, u.username, r.opening_time, r.closing_time " +
                "FROM restaurants r LEFT JOIN users u ON r.user_id = u.id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= 6; i++) row.add(rs.getString(i) != null ? rs.getString(i) : "");
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        restaurantsTable.setItems(data);
    }

    private void setupDishesTable() {
        colDishId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colDishName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colDishDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colDishPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
    }

    private void loadDishes(String restaurantId) {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = "SELECT id, name, describtion, price FROM menu_items WHERE restaurant_id = " + restaurantId;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= 4; i++) row.add(rs.getString(i) != null ? rs.getString(i) : "");
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        dishesTable.setItems(data);
    }

    @FXML private void handleAddRestaurant() { showInfo("Edit Message", "Feature coming soon."); }

    @FXML private void handleRefreshRestaurants() { loadRestaurants(); }

    @FXML private void handleDeleteRestaurant() {
        ObservableList<String> selected = restaurantsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Delete Restaurant", "Please select a restaurant first."); return; }
        confirm("Delete restaurant " + selected.get(1) + "?", () -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM restaurants WHERE id = ?")) {
                stmt.setInt(1, Integer.parseInt(selected.get(0)));
                stmt.executeUpdate();
                loadRestaurants();
                dishesTable.setItems(FXCollections.observableArrayList());
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    @FXML private void handleAddDish() {
        ObservableList<String> selectedRest = restaurantsTable.getSelectionModel().getSelectedItem();
        if (selectedRest == null) {
            showInfo("Add Dish", "Please select a restaurant first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/coursework/add_dish_dialog.fxml"));
            Parent root = loader.load();

            AddDishController controller = loader.getController();
            controller.setRestaurantId(Integer.parseInt(selectedRest.get(0)));
            controller.setOnSuccess(() -> loadDishes(selectedRest.get(0)));

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
            controller.setOnSuccess(() -> {
                ObservableList<String> rest = restaurantsTable.getSelectionModel().getSelectedItem();
                if (rest != null) loadDishes(rest.get(0));
            });

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
                ObservableList<String> rest = restaurantsTable.getSelectionModel().getSelectedItem();
                if (rest != null) loadDishes(rest.get(0));
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    // == ORDERS ==

    private void setupOrdersTable() {
        colOrderId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colOrderCustomer.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colOrderRestaurant.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colOrderDriver.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
        colOrderStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(4)));
        colOrderTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(5)));
        colOrderPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(6)));
    }

    private void loadOrders(String status, String restaurant, String customer, LocalDate date) {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        StringBuilder sql = new StringBuilder(
                "SELECT o.id, cu.username, r.name, dr.username, o.status, o.order_time, o.total_price " +
                        "FROM orders o " +
                        "LEFT JOIN users cu ON o.customer_id = cu.id " +
                        "LEFT JOIN restaurants r ON o.restaurant_id = r.id " +
                        "LEFT JOIN users dr ON o.driver_id = dr.id WHERE 1=1"
        );
        if (status != null && !status.isEmpty() && !status.equals("ALL")) sql.append(" AND o.status = '").append(status).append("'");
        if (restaurant != null && !restaurant.isEmpty()) sql.append(" AND r.name LIKE '%").append(restaurant).append("%'");
        if (customer != null && !customer.isEmpty()) sql.append(" AND cu.username LIKE '%").append(customer).append("%'");
        if (date != null) sql.append(" AND DATE(o.order_time) = '").append(date).append("'");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= 7; i++) row.add(rs.getString(i) != null ? rs.getString(i) : "");
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        ordersTable.setItems(data);
    }

    @FXML private void handleFilterOrders() {
        loadOrders(filterOrderStatus.getValue(), filterOrderRestaurant.getText(), filterOrderCustomer.getText(), filterOrderDate.getValue());
    }
    @FXML private void handleResetOrderFilter() {
        filterOrderStatus.setValue(null); filterOrderRestaurant.clear(); filterOrderCustomer.clear(); filterOrderDate.setValue(null);
        loadOrders(null, null, null, null);
    }

    @FXML private void handleAddOrder() { showInfo("Edit Message", "Feature coming soon."); }

    @FXML private void handleRefreshOrders() { loadOrders(null, null, null, null); }

    @FXML private void handleDeleteOrder() {
        ObservableList<String> selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Delete Order", "Please select an order first."); return; }
        confirm("Delete order #" + selected.get(0) + "?", () -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM orders WHERE id = ?")) {
                stmt.setInt(1, Integer.parseInt(selected.get(0)));
                stmt.executeUpdate();
                loadOrders(null, null, null, null);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    // == MESSAGES ==

    private void setupMessagesTable() {
        colMessageId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colMessageOrder.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colMessageSender.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colMessageText.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
        colMessageTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(4)));
    }

    private void loadMessages() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = "SELECT m.id, m.order_id, u.username, m.message, m.sent_at FROM messages m LEFT JOIN users u ON m.sender_id = u.id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= 5; i++) row.add(rs.getString(i) != null ? rs.getString(i) : "");
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        messagesTable.setItems(data);
    }

    @FXML private void handleNewMessage() { showInfo("Edit Message", "Feature coming soon."); }
    @FXML private void handleRefreshMessages() { loadMessages(); }
    @FXML private void handleEditMessage() { showInfo("Edit Message", "Feature coming soon."); }

    @FXML private void handleDeleteMessage() {
        ObservableList<String> selected = messagesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Delete Message", "Please select a message first."); return; }
        confirm("Delete message #" + selected.get(0) + "?", () -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM messages WHERE id = ?")) {
                stmt.setInt(1, Integer.parseInt(selected.get(0)));
                stmt.executeUpdate();
                loadMessages();
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    // == SUPPORT ==

    private void setupSupportTable() {
        colSupportId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colSupportUser.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colSupportMessage.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colSupportTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
        colSupportStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(4)));
    }

    @FXML private void handleRefreshSupport() { showInfo("Support", "No support messages yet."); }
    @FXML private void handleSupportReply() { showInfo("Reply", "Feature coming soon."); }
    @FXML private void handleSupportDelete() { showInfo("Delete", "Feature coming soon."); }

    // == STATISTICS ==

    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs;

            rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) statTotalUsers.setText(rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER'");
            if (rs.next()) statCustomers.setText(rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'DRIVER'");
            if (rs.next()) statDrivers.setText(rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'RESTAURANT'");
            if (rs.next()) statRestaurantOwners.setText(rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM restaurants");
            if (rs.next()) statRestaurants.setText(rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM orders");
            if (rs.next()) statTotalOrders.setText(rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED'");
            if (rs.next()) statCompletedOrders.setText(rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM orders WHERE status = 'PENDING'");
            if (rs.next()) statPendingOrders.setText(rs.getString(1));

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