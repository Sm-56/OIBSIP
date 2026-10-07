package src;

import javax.swing.*;
import java.awt.*;

public class Profile extends JFrame {

    private User user;

    private JTextField displayNameField;
    private JPasswordField passwordField;

    public Profile(User user) {

        this.user = user;

        setTitle("Student Profile");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        JLabel usernameLabel = new JLabel("Username:");
        JLabel displayNameLabel = new JLabel("Display Name:");
        JLabel passwordLabel = new JLabel("New Password:");

        JLabel usernameValue = new JLabel(user.getUsername());

        displayNameField = new JTextField(
                user.getDisplayName()
        );

        passwordField = new JPasswordField();

        JButton updateButton = new JButton("Update Profile");

        panel.add(usernameLabel);
        panel.add(usernameValue);

        panel.add(displayNameLabel);
        panel.add(displayNameField);

        panel.add(passwordLabel);
        panel.add(passwordField);

        panel.add(new JLabel());
        panel.add(updateButton);

        add(panel);

        updateButton.addActionListener(e -> updateProfile());
    }

    private void updateProfile() {

        String displayName = displayNameField.getText().trim();
        String newPassword =
                new String(passwordField.getPassword());

        if (displayName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Display name cannot be empty."
            );
            return;
        }

        if (!newPassword.isEmpty()) {
            user.setPassword(newPassword);
        }

        user.setDisplayName(displayName);

        JOptionPane.showMessageDialog(
                this,
                "Profile updated successfully!"
        );

        dispose();
    }
}