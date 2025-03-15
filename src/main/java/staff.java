
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class staff extends JFrame {
    private JTable productTable;
    private DefaultTableModel productModel;
    private JButton addProductButton, editProductButton,userButton, deleteProductButton;
public staff(){
    setTitle("Staff Dashboard");
    setSize(700, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    // Table to display products
    // Table to display products
    productTable = new JTable();
    productModel = new DefaultTableModel();
    productModel.setColumnIdentifiers(new Object[]{"ID", "Name", "Description", "Price", "Units","date_listed"});
    productTable.setModel(productModel);
    loadProducts();

    JScrollPane scrollPane = new JScrollPane(productTable);
    add(scrollPane, BorderLayout.CENTER);


    JPanel buttonPanel = new JPanel();
    addProductButton = new JButton("Add Product");
    userButton = new JButton("Load Users");
    editProductButton = new JButton("Edit Product");
    deleteProductButton = new JButton("Delete Product");

    addProductButton.addActionListener(e -> new addproduct(this));
    userButton.addActionListener(e-> new userbase());
    editProductButton.addActionListener(e -> editProduct());
    deleteProductButton.addActionListener(e -> deleteProduct());

    buttonPanel.add(addProductButton);
    buttonPanel.add(userButton);
    buttonPanel.add(editProductButton);
    buttonPanel.add(deleteProductButton);
    add(buttonPanel, BorderLayout.SOUTH);

    setVisible(true);
}

    public void loadProducts() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            String sql = "SELECT id, name, description, price, units_in_stock,created_at FROM products";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            // Clear existing rows
            productModel.setRowCount(0);

            //DefaultTableModel model = new DefaultTableModel();
           // model.setColumnIdentifiers(new Object[]{"ID", "Name", "Description", "Price", "Units"});

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

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching products: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }



    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a product to edit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int productId = (int) productTable.getValueAt(selectedRow, 0);
        new addproduct(this, productId);
    }



    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a product to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int productId = (int) productTable.getValueAt(selectedRow, 0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM products WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadProducts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
