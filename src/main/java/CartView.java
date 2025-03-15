import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CartView extends JFrame {
    private JTable cartTable;
    private JButton removeButton, checkoutButton;
    private int userId;

    public CartView(int userId) {
        this.userId = userId;
        setTitle("Cart");
        setSize(600, 300);
        setLayout(new BorderLayout());

        cartTable = new JTable();
        loadCartItems();

        JScrollPane scrollPane = new JScrollPane(cartTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        removeButton = new JButton("Remove Item");
        checkoutButton = new JButton("Checkout");

        removeButton.addActionListener(e -> removeItemFromCart());
        checkoutButton.addActionListener(e -> checkout(userId));

        buttonPanel.add(removeButton);
        buttonPanel.add(checkoutButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadCartItems() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT c.id, p.name, c.quantity, p.price, (c.quantity * p.price) AS total_price FROM cart c " +
                    "JOIN products p ON c.product_id = p.id WHERE c.user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.setColumnIdentifiers(new Object[]{"Cart ID", "Product", "Quantity", "Price", "Total Price"});

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getInt("quantity"));
                row.add(rs.getDouble("price"));
                row.add(rs.getDouble("total_price"));
                model.addRow(row);
            }

            cartTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeItemFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an item to remove!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cartId = (int) cartTable.getValueAt(selectedRow, 0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM cart WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cartId);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Item removed!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCartItems();  // Refresh cart
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove item!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkout(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if cart has items
            String cartCheckSQL = "SELECT SUM(quantity * price) AS total_amount FROM cart JOIN products ON cart.product_id = products.id WHERE cart.user_id = ?";
            PreparedStatement cartCheckStmt = conn.prepareStatement(cartCheckSQL);
            cartCheckStmt.setInt(1, userId);
            ResultSet cartCheckResult = cartCheckStmt.executeQuery();

            if (!cartCheckResult.next() || cartCheckResult.getDouble("total_amount") == 0) {
                JOptionPane.showMessageDialog(this, "Your cart is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double totalAmount = cartCheckResult.getDouble("total_amount");

            // Simulate payment process
            int confirm = JOptionPane.showConfirmDialog(this, "Total amount: $" + totalAmount + "\nProceed to payment?", "Payment", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Insert order into orders table
            String insertOrderSQL = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, 'Paid')";
            PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
            insertOrderStmt.setInt(1, userId);
            insertOrderStmt.setDouble(2, totalAmount);
            insertOrderStmt.executeUpdate();

            ResultSet generatedKeys = insertOrderStmt.getGeneratedKeys();
            int orderId;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve order ID.");
            }

            // Move cart items to order_items table
            String moveItemsSQL = "INSERT INTO order_items (order_id, product_id, quantity, price) \n" +
                    "SELECT ?, c.product_id, c.quantity, p.price \n" +
                    "FROM cart c \n" +
                    "JOIN products p ON c.product_id = p.id \n" +
                    "WHERE c.user_id = ?\n";
            PreparedStatement moveItemsStmt = conn.prepareStatement(moveItemsSQL);
            moveItemsStmt.setInt(1, orderId);
            moveItemsStmt.setInt(2, userId);
            moveItemsStmt.executeUpdate();

            // Clear the cart
            String clearCartSQL = "DELETE FROM cart WHERE user_id = ?";
            PreparedStatement clearCartStmt = conn.prepareStatement(clearCartSQL);
            clearCartStmt.setInt(1, userId);
            clearCartStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Payment successful! Your order has been placed.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error processing checkout: " + e.getMessage(), "Checkout Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
