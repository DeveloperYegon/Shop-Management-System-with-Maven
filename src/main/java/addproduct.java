import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class addproduct extends JFrame {

    private JTextField nameField, descriptionField, priceField, unitsField, skuField, imageField;
    private JButton saveButton;
    private int productId = -1; // -1 means adding a new product
    private staff parent; // Reference to parent class to refresh product list

    public addproduct(staff parent) {
        this(parent, -1); // Call the constructor for adding a new product
    }

    public addproduct(staff parent, int productId) {
        this.parent = parent;
        this.productId = productId;
        setTitle(productId == -1 ? "Add Product" : "Edit Product");
        setSize(400, 300);
        setLayout(new GridLayout(7, 2));

        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Description:"));
        descriptionField = new JTextField();
        add(descriptionField);

        add(new JLabel("Price:"));
        priceField = new JTextField();
        add(priceField);

        add(new JLabel("Units in Stock:")); // FIXED label
        unitsField = new JTextField();
        add(unitsField);

        add(new JLabel("SKU:"));
        skuField = new JTextField();
        add(skuField);

        add(new JLabel("Product Image URL:")); // FIXED label
        imageField = new JTextField();
        add(imageField);

        saveButton = new JButton(productId == -1 ? "Add Product" : "Update Product");
        saveButton.addActionListener(e -> saveProduct());
        add(saveButton);

        setVisible(true);
    }

    private void saveProduct() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        String price = priceField.getText().trim();
        String units = unitsField.getText().trim();
        String sku = skuField.getText().trim();
        String image = imageField.getText().trim();

        // Ensure required fields are filled
        if (name.isEmpty() || price.isEmpty() || units.isEmpty() || sku.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            if (productId == -1) {
                // INSERT new product
                sql = "INSERT INTO products (name, description, price, units_in_stock, sku, image_url) VALUES (?, ?, ?, ?, ?, ?)";
            } else {
                // UPDATE existing product
                sql = "UPDATE products SET name=?, description=?, price=?, units_in_stock=?, sku=?, image_url=? WHERE id=?";
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setDouble(3, Double.parseDouble(price)); // Ensure correct data type
            stmt.setInt(4, Integer.parseInt(units)); // Ensure correct data type
            stmt.setString(5, sku);
            stmt.setString(6, image);

            if (productId != -1) {
                stmt.setInt(7, productId); // FIXED index error (was 6)
            }

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            parent.loadProducts(); // Refresh product list after save
            dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input for price or units. Please enter numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
