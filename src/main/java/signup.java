
import org.mindrot.jbcrypt.BCrypt;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
public class signup extends JPanel{

    private JTextField nameField, emailField, locationField;
    private JPasswordField passwordField;
    private JComboBox<String> genderBox;
    private JButton signUpButton, loginbtn;

    public signup(){
        setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Sign Up", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 1));
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Location:"));
        locationField = new JTextField();
        formPanel.add(locationField);

        formPanel.add(new JLabel("Gender:"));
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        formPanel.add(genderBox);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        JPanel buttonPanel = new JPanel();
        signUpButton = new JButton("Sign Up");
        buttonPanel.add(signUpButton);
        loginbtn =new JButton("Login");
        buttonPanel.add(loginbtn);

        add(titleLabel,BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel,BorderLayout.SOUTH);


        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        loginbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loginInstead();
            }
        });

        setVisible(true);
    }
    private boolean isEmailRegistered(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            var rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // Email exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void registerUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String location = locationField.getText();
        String gender = genderBox.getSelectedItem().toString();
        String password = new String(passwordField.getPassword());

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isEmailRegistered(email)) {
            JOptionPane.showMessageDialog(this, "Email already registered!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO users (name, email, location, gender, password_hash, role) VALUES (?, ?, ?, ?, ?, 'customer')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, location);
            stmt.setString(4, gender);
            stmt.setString(5, hashedPassword);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Sign Up Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            new login();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error signing up!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loginInstead() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loginFrame.setSize(400, 300);
        loginFrame.setLocationRelativeTo(null);

        login loginPanel = new login();
        loginFrame.add(loginPanel);

        loginFrame.setVisible(true);
    }



    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        locationField.setText("");
        genderBox.setSelectedIndex(0);
        passwordField.setText("");
    }


}
