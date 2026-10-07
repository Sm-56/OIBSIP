# Online Examination System (Java Task 4)

This is an Online Examination System built as part of the Oasis Infobyte Java Development Internship (Task 4). It is a desktop and web-based application designed to facilitate secure and user-friendly online testing.

## Features

- **Login Authentication**: Secure login with a username and password.
- **Profile Update**: Users can update their display name and password before starting the exam.
- **Exam Interface**: 
  - Multiple-choice questions with radio-button selection.
  - Previous and Next navigation buttons.
  - Real-time countdown timer.
- **Automatic & Manual Submission**:
  - Exam auto-submits when the timer reaches `00:00`.
  - Manual submission with a confirmation dialog.
- **Comprehensive Result Page**:
  - Score display and percentage.
  - Correct and Incorrect answer counts.
  - Detailed Answer Review showing selected vs correct options.
  - Total time taken.
- **Window/Browser Close Protection**: Prevents accidental closure during an active exam.

## Technologies Used

- **Java (JDK)**: Core business logic and backend.
- **Java built-in `HttpServer`**: Lightweight REST API and web server (no external dependencies required).
- **HTML, CSS, JavaScript (Vanilla)**: For the modern, responsive web frontend.
- *(Optional)* **Java Swing**: Original desktop UI is preserved and can be enabled if preferred.

## Project Structure

```
OIBSIP/
└── Java-Task4-OnlineExaminationSystem/
    ├── README.md
    ├── screenshots/
    └── src/
        ├── Exam.java
        ├── Login.java
        ├── Main.java
        ├── Profile.java
        ├── Question.java
        ├── Result.java
        ├── User.java
        ├── WebServer.java
        └── web/
            ├── app.js
            ├── index.html
            └── style.css
```

## How to Run

Since this project requires zero external dependencies, you can compile and run it easily using the standard Java Development Kit (JDK).

**1. Compile the project:**
Open your terminal in the `Java-Task4-OnlineExaminationSystem` directory and run:
```bash
javac src/*.java
```

**2. Start the Application / Web Server:**
```bash
java src.Main
```

**3. Open the Browser:**
Once the server starts, it will display a URL. Open this in your web browser:
[http://localhost:8080](http://localhost:8080)

## Login Credentials

To test the application, use the default test account:
- **Username**: `saniya`
- **Password**: `1234`

## Screenshots

*(Please add screenshots of the following screens to the `screenshots` folder and link them here)*
- Login Screen
- Profile Screen
- Active Exam Screen (with timer)
- Submit Confirmation
- Result Screen (with detailed answer review)
