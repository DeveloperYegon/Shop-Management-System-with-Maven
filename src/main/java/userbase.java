import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
public class userbase extends JFrame {
    private JTable userTable;
    private JButton updateRoleButton;
    public userbase(){
        setTitle("User Management");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        userTable = new JTable();
        loadUsers();

        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        updateRoleButton = new JButton("Update Role");
        updateRoleButton.addActionListener(e -> updateRole());
        add(updateRoleButton, BorderLayout.SOUTH);

        setVisible(true);
    }
    private void loadUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name, email, role FROM users";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.setColumnIdentifiers(new Object[]{"ID", "Name", "Email", "Role"});

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("role"));
                model.addRow(row);
            }

            userTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateRole() {
        JOptionPane.showMessageDialog(this, "Feature Coming Soon!", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
