public class Account {

    private String userId;
    private int pin;
    private double balance;

    public Account(String userId, int pin, double balance) {
        this.userId = userId;
        this.pin = pin;
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public boolean checkPin(int enteredPin) {
        return this.pin == enteredPin;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposit successful!");
        } else {
            System.out.println("Invalid amount!");
        }
    }

    public boolean withdraw(double amount) {

        if (amount <= 0) {
            System.out.println("Invalid amount!");
            return false;
        }

        if (amount > balance) {
            System.out.println("Insufficient Funds!");
            return false;
        }

        balance -= amount;
        System.out.println("Withdrawal successful!");
        return true;
    }
}