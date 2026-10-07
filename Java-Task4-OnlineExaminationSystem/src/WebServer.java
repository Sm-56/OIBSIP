package src;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WebServer {

    private static User currentUser;
    private static ArrayList<Question> questions;
    private static final Map<String, String> sessions = new HashMap<>(); // simple session token -> username

    public static void start(int port) throws IOException {
        currentUser = new User("saniya", "1234", "Saniya");
        createQuestions();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/profile", new ProfileHandler());
        server.createContext("/api/questions", new QuestionsHandler());
        server.createContext("/api/submit", new SubmitHandler());
        server.createContext("/api/logout", new LogoutHandler());

        server.setExecutor(null);
        server.start();
    }

    private static void createQuestions() {
        questions = new ArrayList<>();
        questions.add(new Question("Which language is mainly used in this project?", new String[]{"Java", "Python", "HTML", "SQL"}, 0));
        questions.add(new Question("Which keyword is used to create a class in Java?", new String[]{"function", "class", "define", "object"}, 1));
        questions.add(new Question("Which component is used for a clickable button in Swing?", new String[]{"JButton", "JLabel", "JTextField", "JPanel"}, 0));
        questions.add(new Question("Which method starts a Java application?", new String[]{"start()", "run()", "main()", "execute()"}, 2));
        questions.add(new Question("Which collection stores elements in a resizable list?", new String[]{"ArrayList", "HashMap", "TreeSet", "Queue"}, 0));
        questions.add(new Question("Which Swing component allows selecting one option from a group?", new String[]{"JLabel", "JRadioButton", "JTextArea", "JFrame"}, 1));
        questions.add(new Question("Which keyword is used to inherit a class?", new String[]{"implements", "extends", "inherits", "super"}, 1));
        questions.add(new Question("Which keyword creates an object in Java?", new String[]{"new", "create", "object", "make"}, 0));
        questions.add(new Question("Which method is used to display a Swing window?", new String[]{"showWindow()", "display()", "setVisible()", "open()"}, 2));
        questions.add(new Question("Which symbol is used to end a Java statement?", new String[]{":", ".", ";", ","}, 2));
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            File file = new File("src/web" + path);
            if (file.exists() && !file.isDirectory()) {
                String contentType = "text/plain";
                if (path.endsWith(".html")) contentType = "text/html";
                else if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "application/javascript";

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(file.toPath(), os);
                }
            } else {
                String response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    // A very basic JSON parser/generator for our specific needs to avoid external dependencies
    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().replaceAll("^\"|\"$", "");
                    String value = kv[1].trim().replaceAll("^\"|\"$", "");
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    private static String getBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toString("UTF-8");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] response = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> body = parseJson(getBody(exchange));
                if (currentUser.getUsername().equals(body.get("username")) && currentUser.getPassword().equals(body.get("password"))) {
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                } else {
                    sendJsonResponse(exchange, 401, "{\"success\":false, \"message\":\"Invalid credentials\"}");
                }
            }
        }
    }

    static class ProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String json = String.format("{\"username\":\"%s\", \"displayName\":\"%s\"}", currentUser.getUsername(), currentUser.getDisplayName());
                sendJsonResponse(exchange, 200, json);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> body = parseJson(getBody(exchange));
                String displayName = body.get("displayName");
                String password = body.get("password");
                if (displayName != null && !displayName.isEmpty()) {
                    currentUser.setDisplayName(displayName);
                }
                if (password != null && !password.isEmpty() && !password.equals("null")) {
                    currentUser.setPassword(password);
                }
                sendJsonResponse(exchange, 200, "{\"success\":true}");
            }
        }
    }

    static class QuestionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < questions.size(); i++) {
                    Question q = questions.get(i);
                    json.append(String.format("{\"questionText\":\"%s\", \"options\":[\"%s\",\"%s\",\"%s\",\"%s\"]}",
                            q.getQuestionText(), q.getOptions()[0], q.getOptions()[1], q.getOptions()[2], q.getOptions()[3]));
                    if (i < questions.size() - 1) json.append(",");
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            }
        }
    }

    static class SubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String bodyStr = getBody(exchange);
                bodyStr = bodyStr.replaceAll("\\[|\\]|\\s", "");
                String[] answersStr = bodyStr.split(",");
                int score = 0, correct = 0, incorrect = 0;
                StringBuilder details = new StringBuilder("[");
                for (int i = 0; i < questions.size(); i++) {
                    int ans = -1;
                    if (i < answersStr.length) {
                        try { ans = Integer.parseInt(answersStr[i]); } catch (NumberFormatException e) {}
                    }
                    if (ans == questions.get(i).getCorrectAnswer()) {
                        score++; correct++;
                    } else if (ans != -1) {
                        incorrect++;
                    }
                    details.append(String.format("{\"questionText\":\"%s\", \"userAnswer\":%d, \"correctAnswer\":%d, \"options\":[\"%s\",\"%s\",\"%s\",\"%s\"]}", 
                        questions.get(i).getQuestionText(), ans, questions.get(i).getCorrectAnswer(), 
                        questions.get(i).getOptions()[0], questions.get(i).getOptions()[1], 
                        questions.get(i).getOptions()[2], questions.get(i).getOptions()[3]));
                    if (i < questions.size() - 1) details.append(",");
                }
                details.append("]");
                String json = String.format("{\"score\":%d, \"total\":%d, \"correct\":%d, \"incorrect\":%d, \"details\":%s}", score, questions.size(), correct, incorrect, details.toString());
                sendJsonResponse(exchange, 200, json);
            }
        }
    }

    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, "{\"success\":true}");
            }
        }
    }
}
