package src;

import javax.swing.*;
import java.awt.*;

public class Login extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    private User user;

    public Login() {

        setTitle("Online Examination System - Login");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create default student account
        user = new User(
                "saniya",
                "1234",
                "Saniya"
        );

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.setBorder(
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        );

        JLabel titleLabel = new JLabel(
                "ONLINE EXAMINATION",
                SwingConstants.CENTER
        );

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");

        panel.add(usernameLabel);
        panel.add(usernameField);

        panel.add(passwordLabel);
        panel.add(passwordField);

        panel.add(new JLabel());
        panel.add(loginButton);

        add(titleLabel, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> login());
    }

    private void login() {

        String username = usernameField.getText();

        String password =
                new String(passwordField.getPassword());

        if (username.equals(user.getUsername())
                && password.equals(user.getPassword())) {

            JOptionPane.showMessageDialog(
                    this,
                    "Login Successful!"
            );

            dispose();

            new Exam(user).setVisible(true);

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid username or password!",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}