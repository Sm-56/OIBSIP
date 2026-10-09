import java.time.LocalDateTime;

public class Transaction {

    private String type;
    private double amount;
    private String details;
    private LocalDateTime dateTime;

    public Transaction(String type, double amount, String details) {
        this.type = type;
        this.amount = amount;
        this.details = details;
        this.dateTime = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return dateTime + " | " + type + " | Rs." + amount + " | " + details;
    }
}