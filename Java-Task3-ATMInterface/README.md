# ATM Interface — Java

## Project Overview

This project is an ATM simulation developed in Java using Object-Oriented Programming (OOP) concepts. It provides two independent interfaces that share the same core banking logic:

* **Console Interface** — a command-line ATM (`Main.java`, `ATM.java`, `Account.java`, `Bank.java`, `Transaction.java`).
* **SwiftATM (Browser Interface)** — a browser-based ATM served by a lightweight Java HTTP server (`AtmServer.java`, `AtmService.java`) with a frontend built from the `web/` files (`index.html`, `styles.css`, `app.js`).

Both interfaces support the same accounts and ATM operations.

## Features

### Shared features

* User ID and PIN authentication
* Maximum three login attempts
* View transaction history
* Withdraw cash
* Deposit cash
* Transfer money between accounts
* Display current balance
* Validate receiver accounts and prevent transfers to the same account
* Invalid operation / invalid amount handling
* Insufficient funds handling

### Console interface

* Menu-driven text interface
* Invalid menu choice handling
* Quit the application

### SwiftATM browser interface

* Modern responsive login and dashboard UI
* Session-based authentication using tokens
* Three-attempt lockout with a temporary 30-second lock
* Live balance card and transaction history table
* Quick withdraw / deposit / transfer actions with preset amount chips
* Success and error toasts, session logout

## Technologies Used

* Java (JDK)
* Object-Oriented Programming (OOP)
* `ArrayList`, `HashMap`, `Scanner`
* `com.sun.net.httpserver.HttpServer` (built-in JDK HTTP server, no external dependencies)
* HTML, CSS, and vanilla JavaScript for the browser interface

## Sample Login Credentials

| User ID |  PIN | Initial Balance |
| ------- | ---: | --------------: |
| user01  | 1234 |      Rs. 10,000 |
| user02  | 5678 |       Rs. 5,000 |

These credentials are defined in the source code (`Main.java` and `AtmService.java`) and are intended for local testing only.

## Project Structure

```text
Java-Task3-ATMInterface/
├── src/
│   ├── Main.java          # Console entry point
│   ├── ATM.java           # Console ATM UI and menu logic
│   ├── Account.java       # Account model (PIN, balance, deposit/withdraw)
│   ├── Bank.java          # In-memory account store
│   ├── Transaction.java   # Transaction record model
│   ├── AtmServer.java     # Browser interface: HTTP server and routing
│   └── AtmService.java    # Browser interface: login, sessions, ATM operations
├── web/
│   ├── index.html         # SwiftATM login and dashboard markup
│   ├── styles.css         # SwiftATM styling
│   └── app.js             # SwiftATM frontend logic and API calls
├── screenshots/           # Screenshots of both interfaces
└── README.md
```

## How to Run

Make sure the Java JDK is installed. Open PowerShell in the `Java-Task3-ATMInterface` project folder.

### Option 1 — Browser-based SwiftATM interface

Compile the required sources:

```powershell
javac -d . src\AtmServer.java src\AtmService.java src\Account.java src\Bank.java src\Transaction.java
```

Run the server:

```powershell
java AtmServer
```

Then open the following address in your browser:

```text
http://localhost:8080
```

Press `Ctrl+C` in the terminal to stop the server. The server defaults to port `8080`; you can pass a different port as the first argument, for example `java AtmServer 9000`.

### Option 2 — Java console interface

The console version runs separately from the browser version. Compile and run it with:

```powershell
javac -d . src\Main.java src\ATM.java src\Account.java src\Bank.java src\Transaction.java
java Main
```

Follow the console prompts to log in and select an ATM operation.

## Screenshots

Screenshots of the running interfaces are available in the [`screenshots/`](screenshots) folder, including login, dashboard, withdraw, deposit, transfer, transaction history, and invalid PIN views.

## Objective

To demonstrate a complete ATM workflow and Java OOP concepts through two interfaces — a console application and a browser-based interface — powered by a shared Java backend with no external dependencies.
