import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class customer extends JFrame {
    private JTable productTable;
    private JButton addToCartButton, viewCartButton;
    private DefaultTableModel productModel;
    private int userId; // Store user ID


    public customer(int userID) {
        this.userId = userID; // Store user ID


        setTitle("Products");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table to display products
        productTable = new JTable();
        productModel = new DefaultTableModel();
        productModel.setColumnIdentifiers(new Object[]{"ID", "Name", "Description", "Price", "Units","date_listed"});
        productTable.setModel(productModel);

        loadProducts(); // Load products from database

        JScrollPane scrollPane = new JScrollPane(productTable);



        JPanel buttonPanel = new JPanel();
        addToCartButton = new JButton("Add to Cart");
        addToCartButton.addActionListener(e -> addToCart(userId));
        buttonPanel.add(addToCartButton);


        viewCartButton=new JButton("View Cart");
        viewCartButton.addActionListener(e->new CartView(userId));
        buttonPanel.add(viewCartButton);


        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Center aligns the table properly
        mainPanel.add(buttonPanel, BorderLayout.SOUTH); // Buttons at the bottom
        add(mainPanel);

        setVisible(true);
    }

    private void loadProducts() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name, description, price, units_in_stock,created_at FROM products"; // Fixed column name
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Clear existing rows
            productModel.setRowCount(0);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("description"));
                row.add(rs.getDouble("price"));
                row.add(rs.getInt("units_in_stock"));
                row.add(rs.getDate("created_at"));
                productModel.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching products: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void addToCart(int userId) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int productId = (int) productTable.getValueAt(selectedRow, 0);  // Assuming column 0 is ID
        String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:", "Quantity", JOptionPane.QUESTION_MESSAGE);

        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Quantity cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert product into cart table
            String sql = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Product added to cart!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product to cart!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        new customer(1);
    }


}
