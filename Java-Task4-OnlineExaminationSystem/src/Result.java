package src;
import javax.swing.*;
import java.awt.*;

public class Result extends JFrame {

    public Result(
            JFrame examWindow,
            int score,
            int total,
            int correct,
            int incorrect,
            int timeTaken
    ) {

        setTitle("Exam Result");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(
                BorderFactory.createEmptyBorder(30, 50, 30, 50)
        );

        JLabel titleLabel = new JLabel("EXAM RESULT");
        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 26)
        );
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel(
                "Score: " + score + " out of " + total
        );

        JLabel correctLabel = new JLabel(
                "Correct Answers: " + correct
        );

        JLabel incorrectLabel = new JLabel(
                "Incorrect Answers: " + incorrect
        );

        int minutes = timeTaken / 60;
        int seconds = timeTaken % 60;

        JLabel timeLabel = new JLabel(
                String.format(
                        "Time Taken: %02d:%02d",
                        minutes,
                        seconds
                )
        );

        JButton logoutButton = new JButton("Logout");

        scoreLabel.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        correctLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        incorrectLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));

        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(15));

        panel.add(correctLabel);
        panel.add(Box.createVerticalStrut(10));

        panel.add(incorrectLabel);
        panel.add(Box.createVerticalStrut(10));

        panel.add(timeLabel);
        panel.add(Box.createVerticalStrut(30));

        panel.add(logoutButton);

        add(panel);

        logoutButton.addActionListener(e -> {

            dispose();

            new Login().setVisible(true);
        });
    }
}