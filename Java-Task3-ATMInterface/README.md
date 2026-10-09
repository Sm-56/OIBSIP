# ATM Interface — Java

## Project Overview

A console-based ATM simulation developed in Java using Object-Oriented Programming (OOP) concepts.

## Features

* User ID and PIN authentication
* Maximum three login attempts
* View transaction history
* Withdraw cash
* Deposit cash
* Transfer money between accounts
* Display current balance
* Validate receiver accounts and prevent transfers to the same account
* Invalid menu choice handling
* Quit the application

## Technologies Used

* Java
* Object-Oriented Programming (OOP)
* ArrayList
* Scanner

## Sample Login Credentials

| User ID |  PIN | Initial Balance |
| ------- | ---: | --------------: |
| user01  | 1234 |      Rs. 10,000 |
| user02  | 5678 |       Rs. 5,000 |

These are sample credentials for local testing.

## Project Structure

```text
Java-Task3-ATMInterface/
├── src/
│   ├── Main.java
│   ├── ATM.java
│   ├── Account.java
│   ├── Bank.java
│   └── Transaction.java
├── screenshots/
└── README.md
```

## How to Run

Make sure the Java JDK is installed.

Open a terminal in the `Java-Task3-ATMInterface` project folder and run:

```bash
javac -d . src/*.java
java Main
```

Follow the console prompts to log in and select an ATM operation.

## Screenshots

Console application screenshots can be added to the `screenshots/` folder to document the Java ATM's actual output.

## Objective

To demonstrate a basic ATM workflow and Java OOP concepts through a console-based application.
