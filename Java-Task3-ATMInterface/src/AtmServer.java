import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class AtmServer {

    private static final AtmService SERVICE = new AtmService();
    private static final int MAX_BODY_BYTES = 16 * 1024;

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", AtmServer::handle);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("================================");
        System.out.println("      ATM INTERFACE (WEB)");
        System.out.println("================================");
        System.out.println("Open in browser: http://localhost:" + port);
        System.out.println("Press Ctrl+C to stop.");
    }

    private static void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/api/")) {
                routeApi(exchange, path);
            } else {
                serveStatic(exchange, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"ok\":false,\"message\":\"Internal server error\"}");
        }
    }

    private static void routeApi(HttpExchange exchange, String path) throws IOException {
        String method = exchange.getRequestMethod();
        boolean isPost = "POST".equals(method);
        boolean isGet = "GET".equals(method);
        Map<String, Object> body = isPost ? readBody(exchange) : new HashMap<>();
        String token = token(exchange);

        switch (path) {
            case "/api/login":
                if (!isPost) {
                    methodNotAllowed(exchange);
                    return;
                }
                send(exchange, SERVICE.login(str(body.get("userId")), str(body.get("pin"))));
                return;

            case "/api/state":
                if (!isGet) {
                    methodNotAllowed(exchange);
                    return;
                }
                send(exchange, SERVICE.state(token));
                return;

            case "/api/withdraw":
                if (!isPost) {
                    methodNotAllowed(exchange);
                    return;
                }
                send(exchange, SERVICE.withdraw(token, body.get("amount")));
                return;

            case "/api/deposit":
                if (!isPost) {
                    methodNotAllowed(exchange);
                    return;
                }
                send(exchange, SERVICE.deposit(token, body.get("amount")));
                return;

            case "/api/transfer":
                if (!isPost) {
                    methodNotAllowed(exchange);
                    return;
                }
                send(exchange, SERVICE.transfer(token, str(body.get("receiverId")), body.get("amount")));
                return;

            case "/api/logout":
                if (!isPost) {
                    methodNotAllowed(exchange);
                    return;
                }
                send(exchange, SERVICE.logout(token));
                return;

            default:
                sendJson(exchange, 404, "{\"ok\":false,\"message\":\"Unknown API endpoint\"}");
        }
    }

    private static void send(HttpExchange exchange, AtmService.Response response) throws IOException {
        sendJson(exchange, response.status, response.body);
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    private static void sendText(HttpExchange exchange, int status, String text) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    private static void methodNotAllowed(HttpExchange exchange) throws IOException {
        sendJson(exchange, 405, "{\"ok\":false,\"message\":\"Method not allowed\"}");
    }

    private static String token(HttpExchange exchange) {
        return exchange.getRequestHeaders().getFirst("X-Token");
    }

    private static Map<String, Object> readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int total = 0;
            int read;
            while ((read = in.read(chunk)) != -1) {
                total += read;
                if (total > MAX_BODY_BYTES) {
                    break;
                }
                buffer.write(chunk, 0, read);
            }
            return parseObject(new String(buffer.toByteArray(), StandardCharsets.UTF_8));
        }
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static void serveStatic(HttpExchange exchange, String path) throws IOException {
        Path root = webRoot();
        String relative = (path == null || path.isEmpty() || "/".equals(path)) ? "index.html" : path.substring(1);

        Path file;
        try {
            file = root.resolve(relative).normalize();
        } catch (Exception e) {
            sendText(exchange, 404, "Not Found");
            return;
        }

        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            sendText(exchange, 404, "Not Found");
            return;
        }

        byte[] data = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType(file));
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    private static Path webRoot() {
        String[] candidates = {"web", ".." + java.io.File.separator + "web"};
        for (String candidate : candidates) {
            Path dir = Paths.get(candidate).toAbsolutePath().normalize();
            if (Files.isRegularFile(dir.resolve("index.html"))) {
                return dir;
            }
        }
        try {
            Path code = Paths.get(AtmServer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path base = Files.isDirectory(code) ? code : code.getParent();
            Path dir = base.resolve("web").normalize();
            if (Files.isRegularFile(dir.resolve("index.html"))) {
                return dir;
            }
        } catch (Exception ignored) {
        }
        return Paths.get("web").toAbsolutePath().normalize();
    }

    private static String contentType(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".css")) return "text/css; charset=utf-8";
        if (name.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (name.endsWith(".svg")) return "image/svg+xml";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".ico")) return "image/x-icon";
        if (name.endsWith(".json")) return "application/json; charset=utf-8";
        if (name.endsWith(".txt")) return "text/plain; charset=utf-8";
        return "application/octet-stream";
    }

    private static Map<String, Object> parseObject(String text) {
        Map<String, Object> map = new HashMap<>();
        if (text == null) {
            return map;
        }
        String s = text.trim();
        int n = s.length();
        if (n == 0 || s.charAt(0) != '{') {
            return map;
        }

        int i = 1;
        while (i < n) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c) || c == ',') {
                i++;
                continue;
            }
            if (c == '}' || c != '"') {
                break;
            }

            StringBuilder key = new StringBuilder();
            i = readString(s, i, key);
            if (i < 0) {
                break;
            }

            while (i < n && Character.isWhitespace(s.charAt(i))) i++;
            if (i >= n || s.charAt(i) != ':') {
                break;
            }
            i++;
            while (i < n && Character.isWhitespace(s.charAt(i))) i++;
            if (i >= n) {
                break;
            }

            char valueChar = s.charAt(i);
            if (valueChar == '"') {
                StringBuilder value = new StringBuilder();
                i = readString(s, i, value);
                if (i < 0) {
                    break;
                }
                map.put(key.toString(), value.toString());
            } else if (valueChar == '{' || valueChar == '[') {
                int depth = 0;
                boolean inString = false;
                boolean escaped = false;
                int start = i;
                for (; i < n; i++) {
                    char ch = s.charAt(i);
                    if (inString) {
                        if (escaped) {
                            escaped = false;
                        } else if (ch == '\\') {
                            escaped = true;
                        } else if (ch == '"') {
                            inString = false;
                        }
                    } else if (ch == '"') {
                        inString = true;
                    } else if (ch == '{' || ch == '[') {
                        depth++;
                    } else if (ch == '}' || ch == ']') {
                        depth--;
                        if (depth == 0) {
                            i++;
                            break;
                        }
                    }
                }
                map.put(key.toString(), s.substring(start, i));
            } else {
                int start = i;
                while (i < n && s.charAt(i) != ',' && s.charAt(i) != '}') {
                    i++;
                }
                String token = s.substring(start, i).trim();
                if ("true".equals(token)) {
                    map.put(key.toString(), Boolean.TRUE);
                } else if ("false".equals(token)) {
                    map.put(key.toString(), Boolean.FALSE);
                } else if ("null".equals(token)) {
                    map.put(key.toString(), null);
                } else {
                    try {
                        map.put(key.toString(), Double.valueOf(token));
                    } catch (NumberFormatException e) {
                        map.put(key.toString(), token);
                    }
                }
            }
        }
        return map;
    }

    private static int readString(String s, int start, StringBuilder out) {
        int n = s.length();
        int i = start + 1;
        while (i < n) {
            char c = s.charAt(i);
            if (c == '"') {
                return i + 1;
            }
            if (c == '\\' && i + 1 < n) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case 'b': out.append('\b'); break;
                    case 'f': out.append('\f'); break;
                    case 'u':
                        if (i + 6 <= n) {
                            try {
                                out.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                out.append('u');
                            }
                        }
                        break;
                    default:
                        out.append(next);
                }
                i += 2;
            } else {
                out.append(c);
                i++;
            }
        }
        return -1;
    }
}
