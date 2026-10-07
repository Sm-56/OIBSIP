package src;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Exam extends JFrame {

  private User user;

    private ArrayList<Question> questions;
    private int currentQuestion = 0;

    private int[] selectedAnswers;

    private JLabel questionLabel;
    private JRadioButton option1;
    private JRadioButton option2;
    private JRadioButton option3;
    private JRadioButton option4;

    private ButtonGroup optionGroup;

    private JButton previousButton;
    private JButton nextButton;
    private JButton submitButton;

    private JLabel timerLabel;

    private Timer timer;
    private int timeLeft = 30 * 60; // 30 minutes

  public Exam(User user) {
    this.user = user; 

        setTitle("Online Examination System");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        createQuestions();

        selectedAnswers = new int[questions.size()];

        for (int i = 0; i < selectedAnswers.length; i++) {
            selectedAnswers[i] = -1;
        }

        createGUI();

        startTimer();

        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {

                int choice = JOptionPane.showConfirmDialog(
                        Exam.this,
                        "Are you sure you want to quit the exam?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    timer.stop();
                    System.exit(0);
                }
            }
        });
    }

    private void createQuestions() {

        questions = new ArrayList<>();

        questions.add(new Question(
                "Which language is mainly used in this project?",
                new String[]{"Java", "Python", "HTML", "SQL"},
                0
        ));

        questions.add(new Question(
                "Which keyword is used to create a class in Java?",
                new String[]{"function", "class", "define", "object"},
                1
        ));

        questions.add(new Question(
                "Which component is used for a clickable button in Swing?",
                new String[]{"JButton", "JLabel", "JTextField", "JPanel"},
                0
        ));

        questions.add(new Question(
                "Which method starts a Java application?",
                new String[]{"start()", "run()", "main()", "execute()"},
                2
        ));

        questions.add(new Question(
                "Which collection stores elements in a resizable list?",
                new String[]{"ArrayList", "HashMap", "TreeSet", "Queue"},
                0
        ));

        questions.add(new Question(
                "Which Swing component allows selecting one option from a group?",
                new String[]{"JLabel", "JRadioButton", "JTextArea", "JFrame"},
                1
        ));

        questions.add(new Question(
                "Which keyword is used to inherit a class?",
                new String[]{"implements", "extends", "inherits", "super"},
                1
        ));

        questions.add(new Question(
                "Which keyword creates an object in Java?",
                new String[]{"new", "create", "object", "make"},
                0
        ));

        questions.add(new Question(
                "Which method is used to display a Swing window?",
                new String[]{"showWindow()", "display()", "setVisible()", "open()"},
                2
        ));

        questions.add(new Question(
                "Which symbol is used to end a Java statement?",
                new String[]{":", ".", ";", ","},
                2
        ));
    }

    private void createGUI() {

        setLayout(new BorderLayout(10, 10));

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel userLabel = new JLabel(
            " Student: " + user.getDisplayName()
        );

        timerLabel = new JLabel(
                "Time Left: 30:00"
        );

        timerLabel.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        topPanel.add(userLabel, BorderLayout.WEST);
        topPanel.add(timerLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Question panel
        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(
                new BoxLayout(questionPanel, BoxLayout.Y_AXIS)
        );

        questionLabel = new JLabel();
        questionLabel.setFont(
                new Font("Arial", Font.BOLD, 18)
        );

        option1 = new JRadioButton();
        option2 = new JRadioButton();
        option3 = new JRadioButton();
        option4 = new JRadioButton();

        optionGroup = new ButtonGroup();

        optionGroup.add(option1);
        optionGroup.add(option2);
        optionGroup.add(option3);
        optionGroup.add(option4);

        questionPanel.add(questionLabel);
        questionPanel.add(Box.createVerticalStrut(20));
        questionPanel.add(option1);
        questionPanel.add(option2);
        questionPanel.add(option3);
        questionPanel.add(option4);

        add(questionPanel, BorderLayout.CENTER);

        // Bottom buttons
        JPanel buttonPanel = new JPanel();

        previousButton = new JButton("Previous");
        nextButton = new JButton("Next");
        submitButton = new JButton("Submit Exam");

        buttonPanel.add(previousButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(submitButton);

        add(buttonPanel, BorderLayout.SOUTH);

        previousButton.addActionListener(e -> previousQuestion());

        nextButton.addActionListener(e -> nextQuestion());

        submitButton.addActionListener(e -> confirmSubmit());

        displayQuestion();
    }

    private void displayQuestion() {

        Question q = questions.get(currentQuestion);

        questionLabel.setText(
                "Question " + (currentQuestion + 1)
                        + " of " + questions.size()
                        + ": " + q.getQuestionText()
        );

        String[] options = q.getOptions();

        option1.setText(options[0]);
        option2.setText(options[1]);
        option3.setText(options[2]);
        option4.setText(options[3]);

        optionGroup.clearSelection();

        if (selectedAnswers[currentQuestion] == 0) {
            option1.setSelected(true);
        } else if (selectedAnswers[currentQuestion] == 1) {
            option2.setSelected(true);
        } else if (selectedAnswers[currentQuestion] == 2) {
            option3.setSelected(true);
        } else if (selectedAnswers[currentQuestion] == 3) {
            option4.setSelected(true);
        }

        previousButton.setEnabled(currentQuestion > 0);

        nextButton.setEnabled(
                currentQuestion < questions.size() - 1
        );
    }

    private void saveAnswer() {

        if (option1.isSelected()) {
            selectedAnswers[currentQuestion] = 0;
        } else if (option2.isSelected()) {
            selectedAnswers[currentQuestion] = 1;
        } else if (option3.isSelected()) {
            selectedAnswers[currentQuestion] = 2;
        } else if (option4.isSelected()) {
            selectedAnswers[currentQuestion] = 3;
        }
    }

    private void nextQuestion() {

        saveAnswer();

        if (currentQuestion < questions.size() - 1) {
            currentQuestion++;
            displayQuestion();
        }
    }

    private void previousQuestion() {

        saveAnswer();

        if (currentQuestion > 0) {
            currentQuestion--;
            displayQuestion();
        }
    }

    private void startTimer() {

        timer = new Timer(1000, e -> {

            timeLeft--;

            int minutes = timeLeft / 60;
            int seconds = timeLeft % 60;

            timerLabel.setText(
                    String.format(
                            "Time Left: %02d:%02d",
                            minutes,
                            seconds
                    )
            );

            if (timeLeft <= 0) {

                timer.stop();

                JOptionPane.showMessageDialog(
                        this,
                        "Time is over! Your exam will be submitted automatically."
                );

                submitExam();
            }
        });

        timer.start();
    }

    private void confirmSubmit() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to submit the exam?",
                "Confirm Submission",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            submitExam();
        }
    }

    private void submitExam() {

        saveAnswer();

        if (timer != null) {
            timer.stop();
        }

        int score = 0;
        int correct = 0;
        int incorrect = 0;

        for (int i = 0; i < questions.size(); i++) {

            if (selectedAnswers[i] == questions.get(i).getCorrectAnswer()) {
                score++;
                correct++;
            } else {
                incorrect++;
            }
        }

        int timeTaken = (30 * 60) - timeLeft;

        new Result(
                this,
                score,
                questions.size(),
                correct,
                incorrect,
                timeTaken
        ).setVisible(true);

        dispose();
    }
}