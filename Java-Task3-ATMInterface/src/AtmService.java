import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AtmService {

    public static class Response {
        public final int status;
        public final String body;

        public Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MS = 30_000L;
    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a", Locale.ENGLISH);

    private final Bank bank = new Bank();
    private final Map<String, List<Transaction>> transactions = new HashMap<>();
    private final Map<String, String> sessions = new HashMap<>();
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Map<String, Long> lockedUntil = new HashMap<>();
    private final Object lock = new Object();

    public AtmService() {
        createAccount("user01", 1234, 10000);
        createAccount("user02", 5678, 5000);
    }

    private void createAccount(String userId, int pin, double balance) {
        bank.addAccount(new Account(userId, pin, balance));
        transactions.put(userId, new ArrayList<>());
    }

    public Response login(String userId, String pin) {
        synchronized (lock) {
            if (isBlank(userId) || isBlank(pin)) {
                return error(400, "User ID and PIN are required.");
            }

            userId = userId.trim();
            Long unlockAt = lockedUntil.get(userId);

            if (unlockAt != null) {
                long now = System.currentTimeMillis();
                if (now < unlockAt) {
                    long secondsLeft = (unlockAt - now) / 1000 + 1;
                    return error(423, "Too many incorrect attempts. Try again in " + secondsLeft + " second(s).");
                }
                lockedUntil.remove(userId);
                failedAttempts.remove(userId);
            }

            Account account = bank.findAccount(userId);
            int enteredPin = parsePin(pin);
            boolean valid = account != null && enteredPin >= 0 && account.checkPin(enteredPin);

            if (valid) {
                failedAttempts.remove(userId);
                String token = UUID.randomUUID().toString();
                sessions.put(token, userId);
                return success(
                    "\"token\":\"" + token + "\"," +
                    "\"userId\":\"" + escape(userId) + "\""
                );
            }

            int attempts = failedAttempts.getOrDefault(userId, 0) + 1;
            int remaining = MAX_ATTEMPTS - attempts;

            if (remaining <= 0) {
                failedAttempts.put(userId, 0);
                lockedUntil.put(userId, System.currentTimeMillis() + LOCK_DURATION_MS);
                return error(423, "Too many incorrect attempts. Account access denied.");
            }

            failedAttempts.put(userId, attempts);

            if (account == null) {
                return error(401, "User ID not found. Attempts remaining: " + remaining);
            }
            return error(401, "Incorrect PIN! Attempts remaining: " + remaining);
        }
    }

    public Response state(String token) {
        synchronized (lock) {
            String userId = currentUser(token);
            if (userId == null) {
                return error(401, "Session expired. Please sign in again.");
            }

            Account account = bank.findAccount(userId);
            StringBuilder json = new StringBuilder(256);
            json.append("{\"ok\":true,");
            json.append("\"userId\":\"").append(escape(userId)).append("\",");
            json.append("\"balance\":").append(money(account.getBalance())).append(",");
            json.append("\"transactions\":[");

            List<Transaction> history = transactions.get(userId);
            int shown = 0;
            for (int i = history.size() - 1; i >= 0; i--) {
                Transaction t = history.get(i);
                if (shown++ > 0) {
                    json.append(',');
                }
                json.append("{\"time\":\"").append(escape(t.getDateTime().format(TIME_FORMAT))).append("\",");
                json.append("\"type\":\"").append(escape(t.getType())).append("\",");
                json.append("\"amount\":").append(money(t.getAmount())).append(',');
                json.append("\"details\":\"").append(escape(t.getDetails())).append("\"}");
            }

            json.append("]}");
            return new Response(200, json.toString());
        }
    }

    public Response withdraw(String token, Object rawAmount) {
        synchronized (lock) {
            String userId = currentUser(token);
            if (userId == null) {
                return error(401, "Session expired. Please sign in again.");
            }

            double amount = toAmount(rawAmount);
            if (Double.isNaN(amount) || amount <= 0) {
                return error(400, "Invalid amount!");
            }

            Account account = bank.findAccount(userId);
            if (amount > account.getBalance()) {
                return error(409, "Insufficient Funds!");
            }

            account.withdraw(amount);
            addTransaction(userId, "Withdraw", amount, "Cash withdrawn");

            return success(
                "\"message\":\"Withdrawal successful!\"," +
                "\"balance\":" + money(account.getBalance())
            );
        }
    }

    public Response deposit(String token, Object rawAmount) {
        synchronized (lock) {
            String userId = currentUser(token);
            if (userId == null) {
                return error(401, "Session expired. Please sign in again.");
            }

            double amount = toAmount(rawAmount);
            if (Double.isNaN(amount) || amount <= 0) {
                return error(400, "Invalid amount!");
            }

            Account account = bank.findAccount(userId);
            account.deposit(amount);
            addTransaction(userId, "Deposit", amount, "Cash deposited");

            return success(
                "\"message\":\"Deposit successful!\"," +
                "\"balance\":" + money(account.getBalance())
            );
        }
    }

    public Response transfer(String token, String receiverId, Object rawAmount) {
        synchronized (lock) {
            String userId = currentUser(token);
            if (userId == null) {
                return error(401, "Session expired. Please sign in again.");
            }

            double amount = toAmount(rawAmount);
            if (Double.isNaN(amount) || amount <= 0) {
                return error(400, "Invalid amount!");
            }

            if (isBlank(receiverId)) {
                return error(400, "Receiver User ID is required.");
            }
            receiverId = receiverId.trim();

            Account sender = bank.findAccount(userId);
            Account receiver = bank.findAccount(receiverId);

            if (receiver == null) {
                return error(404, "Receiver account not found!");
            }
            if (receiver == sender) {
                return error(400, "You cannot transfer money to yourself!");
            }
            if (amount > sender.getBalance()) {
                return error(409, "Insufficient Funds!");
            }

            sender.withdraw(amount);
            receiver.deposit(amount);

            addTransaction(sender.getUserId(), "Transfer", amount, "Transferred to " + receiverId);
            addTransaction(receiverId, "Received", amount, "Received from " + userId);

            return success(
                "\"message\":\"Transfer successful!\"," +
                "\"balance\":" + money(sender.getBalance())
            );
        }
    }

    public Response logout(String token) {
        synchronized (lock) {
            if (!isBlank(token)) {
                sessions.remove(token);
            }
            return success("\"message\":\"Logged out successfully!\"");
        }
    }

    private String currentUser(String token) {
        if (isBlank(token)) {
            return null;
        }
        return sessions.get(token);
    }

    private void addTransaction(String userId, String type, double amount, String details) {
        List<Transaction> history = transactions.get(userId);
        if (history != null) {
            history.add(new Transaction(type, amount, details));
        }
    }

    private static Response success(String fields) {
        return new Response(200, "{\"ok\":true," + fields + "}");
    }

    private static Response error(int status, String message) {
        return new Response(status, "{\"ok\":false,\"message\":\"" + escape(message) + "\"}");
    }

    private static double toAmount(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble(((String) value).trim());
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
        return Double.NaN;
    }

    private static int parsePin(String pin) {
        String value = pin.trim();
        if (value.isEmpty() || value.length() > 9) {
            return -1;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return -1;
            }
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String money(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
