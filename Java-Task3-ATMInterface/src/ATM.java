import java.util.ArrayList;
import java.util.Scanner;

public class ATM {

    private Bank bank;
    private Scanner scanner;
    private ArrayList<Transaction> transactions;

    public ATM(Bank bank) {
        this.bank = bank;
        this.scanner = new Scanner(System.in);
        this.transactions = new ArrayList<>();
    }

    public void start() {

        System.out.println("================================");
        System.out.println("        WELCOME TO ATM");
        System.out.println("================================");

        Account currentAccount = login();

        if (currentAccount == null) {
            System.out.println("Too many incorrect attempts.");
            System.out.println("Account access denied.");
            return;
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + currentAccount.getUserId());
        System.out.println("Current Balance: Rs." + currentAccount.getBalance());

        showMenu(currentAccount);
    }

    private Account login() {

        for (int attempt = 1; attempt <= 3; attempt++) {

            System.out.print("\nEnter User ID: ");
            String userId = scanner.nextLine();

            Account account = bank.findAccount(userId);

            if (account == null) {
                System.out.println("User ID not found.");
                continue;
            }

            System.out.print("Enter PIN: ");
            int pin = scanner.nextInt();
            scanner.nextLine();

            if (account.checkPin(pin)) {
                return account;
            }

            System.out.println("Incorrect PIN!");
            System.out.println("Attempts remaining: " + (3 - attempt));
        }

        return null;
    }

    private void showMenu(Account account) {

        while (true) {

            System.out.println("\n================================");
            System.out.println("          ATM MENU");
            System.out.println("================================");
            System.out.println("1. Transaction History");
            System.out.println("2. Withdraw");
            System.out.println("3. Deposit");
            System.out.println("4. Transfer");
            System.out.println("5. Quit");
            System.out.println("================================");

            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1:
                    showTransactionHistory();
                    break;

                case 2:
                    withdraw(account);
                    break;

                case 3:
                    deposit(account);
                    break;

                case 4:
                    transfer(account);
                    break;

                case 5:
                    System.out.println("\nThank you for using our ATM!");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private void withdraw(Account account) {

        System.out.print("Enter amount to withdraw: ");

        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (account.withdraw(amount)) {

            transactions.add(
                new Transaction(
                    "Withdraw",
                    amount,
                    "Cash withdrawn"
                )
            );

            System.out.println(
                "Current Balance: Rs." + account.getBalance()
            );
        }
    }

    private void deposit(Account account) {

        System.out.print("Enter amount to deposit: ");

        double amount = scanner.nextDouble();
        scanner.nextLine();

        double oldBalance = account.getBalance();

        account.deposit(amount);

        if (account.getBalance() > oldBalance) {

            transactions.add(
                new Transaction(
                    "Deposit",
                    amount,
                    "Cash deposited"
                )
            );

            System.out.println(
                "Current Balance: Rs." + account.getBalance()
            );
        }
    }

    private void transfer(Account sender) {

        System.out.print("Enter receiver User ID: ");

        String receiverId = scanner.nextLine();

        Account receiver = bank.findAccount(receiverId);

        if (receiver == null) {
            System.out.println("Receiver account not found!");
            return;
        }

        if (receiver == sender) {
            System.out.println(
                "You cannot transfer money to yourself!"
            );
            return;
        }

        System.out.print("Enter amount to transfer: ");

        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (sender.withdraw(amount)) {

            receiver.deposit(amount);

            transactions.add(
                new Transaction(
                    "Transfer",
                    amount,
                    "Transferred to " + receiverId
                )
            );

            System.out.println("Transfer successful!");
            System.out.println(
                "Current Balance: Rs." + sender.getBalance()
            );
        }
    }

    private void showTransactionHistory() {

        System.out.println("\n================================");
        System.out.println("       TRANSACTION HISTORY");
        System.out.println("================================");

        if (transactions.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }

        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }
}