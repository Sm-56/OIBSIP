public class Main {

    public static void main(String[] args) {

        // Create bank
        Bank bank = new Bank();

        // Create sample accounts
        Account account1 = new Account(
            "user01",
            1234,
            10000
        );

        Account account2 = new Account(
            "user02",
            5678,
            5000
        );

        // Add accounts to bank
        bank.addAccount(account1);
        bank.addAccount(account2);

        // Start ATM
        ATM atm = new ATM(bank);

        atm.start();
    }
}