
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;


public class login extends JPanel {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;


    public login(){
        setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 1));
       formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);


        JPanel buttonpanel =new JPanel();
        loginButton = new JButton("Login");
        buttonpanel.add(loginButton);

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonpanel, BorderLayout.SOUTH);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticateUser();
            }
        });

        setVisible(true);
    }
    private void authenticateUser() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, password_hash, role FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");  // Get the logged-in user ID
                String hashedPassword = rs.getString("password_hash");
                String role = rs.getString("role");

                if (BCrypt.checkpw(password, hashedPassword)) {
                    JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    if (role.equals("customer")) {
                        new customer(userId);
                    } else {
                        new staff();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "User not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error logging in!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void clearForm() {
        emailField.setText("");
        passwordField.setText("");

    }


}
