import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Concurrency Test for O2O Takeout System
 * - Part A: Anti-oversell test (50 concurrent orders, stock=10)
 * - Part B: Performance test (50 threads x 10 requests = 500 GET /api/dishes)
 * Run: java ConcurrencyTest.java
 */
public class ConcurrencyTest {

    static final String BASE = "http://localhost:8080";
    static final int THREAD_COUNT = 50;
    static final int DISH_ID = 13;
    static final int TARGET_STOCK = 10;

    public static void main(String[] args) throws Exception {
        System.out.println("=== O2O Concurrency Test ===");
        System.out.println("Threads: " + THREAD_COUNT + ", Target Stock: " + TARGET_STOCK);
        System.out.println();

        // Part A: Anti-oversell test
        System.out.println("--- Part A: Anti-Oversell Test ---");
        antiOversellTest();
        System.out.println();

        // Part B: Performance test
        System.out.println("--- Part B: Performance Test (50 threads x 10 requests) ---");
        performanceTest();
    }

    static void antiOversellTest() throws Exception {
        // Step 1: Admin login and set stock to TARGET_STOCK
        String adminCookie = login(BASE + "/api/admin/login", "admin", "123456");
        System.out.println("[1] Admin logged in, cookie: " + adminCookie.substring(0, Math.min(30, adminCookie.length())) + "...");

        String stockBody = "{\"targetStock\":" + TARGET_STOCK + "}";
        String stockResp = httpRequest("PUT", BASE + "/api/admin/dishes/" + DISH_ID + "/stock", stockBody, adminCookie);
        System.out.println("[2] Set dish " + DISH_ID + " stock to " + TARGET_STOCK + " -> " + stockResp);

        // Step 2: Register 50 users, login, create address, add dish to cart
        System.out.println("[3] Registering 50 test users...");
        String[] cookies = new String[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            String username = String.format("loadtest%03d", i + 1);
            String phone = String.format("139%08d", i + 1);
            String regBody = "{\"username\":\"" + username + "\",\"phone\":\"" + phone + "\",\"password\":\"123456\"}";
            httpRequest("POST", BASE + "/api/user/register", regBody, null);

            // Login
            cookies[i] = login(BASE + "/api/user/login", username, "123456");

            // Create address
            String addrBody = "{\"consignee\":\"User" + (i+1) + "\",\"phone\":\"" + phone + "\",\"region\":\"Shanghai\",\"detail\":\"Addr " + (i+1) + "\",\"isDefault\":1}";
            httpRequest("POST", BASE + "/api/addresses", addrBody, cookies[i]);

            // Add dish to cart
            String cartBody = "{\"dishId\":" + DISH_ID + ",\"quantity\":1}";
            httpRequest("POST", BASE + "/api/cart", cartBody, cookies[i]);
        }
        System.out.println("[4] All 50 users registered, addresses created, carts loaded.");

        // Step 3: Get each user's address ID
        long[] addressIds = new long[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            String addrResp = httpRequest("GET", BASE + "/api/addresses", null, cookies[i]);
            // Parse first address id from response
            addressIds[i] = parseFirstId(addrResp);
        }
        System.out.println("[5] Address IDs retrieved.");

        // Step 4: Concurrent order submission
        System.out.println("[6] Launching 50 concurrent order requests...");
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> failMessages = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREAD_COUNT);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int idx = i;
            final String cookie = cookies[i];
            final long addrId = addressIds[i];
            new Thread(() -> {
                try {
                    ready.countDown();
                    go.await(); // Wait for all threads to be ready
                    String orderBody = "{\"addressId\":" + addrId + ",\"payMethod\":1}";
                    String resp = httpRequest("POST", BASE + "/api/orders", orderBody, cookie);
                    if (resp.contains("\"code\":1")) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                        failMessages.add(resp);
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    failMessages.add(e.getMessage());
                } finally {
                    done.countDown();
                }
            }).start();
        }

        ready.await(); // Wait for all threads ready
        go.countDown(); // Fire!
        done.await();   // Wait for all to finish
        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("[7] Results:");
        System.out.println("    Success orders: " + successCount.get());
        System.out.println("    Failed orders:  " + failCount.get());
        System.out.println("    Total time:     " + elapsed + " ms");

        // Print a sample fail message
        if (!failMessages.isEmpty()) {
            System.out.println("    Sample fail msg: " + failMessages.get(0).substring(0, Math.min(150, failMessages.get(0).length())));
        }

        // Step 5: Verify final stock
        String dishResp = httpRequest("GET", BASE + "/api/dishes/" + DISH_ID, null, null);
        System.out.println("[8] Final dish " + DISH_ID + " state: " + dishResp);

        // Parse stock from response
        int finalStock = parseStock(dishResp);
        System.out.println("[9] Final stock: " + finalStock);
        System.out.println("    ASSERTION (success == " + TARGET_STOCK + "): " + (successCount.get() == TARGET_STOCK ? "PASS" : "FAIL (got " + successCount.get() + ")"));
        System.out.println("    ASSERTION (fail == " + (THREAD_COUNT - TARGET_STOCK) + "): " + (failCount.get() == (THREAD_COUNT - TARGET_STOCK) ? "PASS" : "FAIL (got " + failCount.get() + ")"));
        System.out.println("    ASSERTION (finalStock == 0): " + (finalStock == 0 ? "PASS" : "FAIL (got " + finalStock + ")"));
        System.out.println("    ASSERTION (no negative stock): " + (finalStock >= 0 ? "PASS" : "FAIL"));
    }

    static void performanceTest() throws Exception {
        int requestsPerThread = 10;
        int totalRequests = THREAD_COUNT * requestsPerThread;
        long[] responseTimes = new long[totalRequests];
        AtomicInteger idx = new AtomicInteger(0);
        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREAD_COUNT);
        AtomicInteger errors = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();
        for (int t = 0; t < THREAD_COUNT; t++) {
            new Thread(() -> {
                try {
                    ready.countDown();
                    go.await();
                    for (int r = 0; r < requestsPerThread; r++) {
                        long s = System.currentTimeMillis();
                        try {
                            String resp = httpRequest("GET", BASE + "/api/dishes?page=1&pageSize=10", null, null);
                            if (!resp.contains("\"code\":1")) errors.incrementAndGet();
                        } catch (Exception e) {
                            errors.incrementAndGet();
                        }
                        long e = System.currentTimeMillis() - s;
                        int i = idx.getAndIncrement();
                        if (i < responseTimes.length) responseTimes[i] = e;
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        ready.await();
        go.countDown();
        done.await();
        long totalElapsed = System.currentTimeMillis() - startTime;

        // Calculate stats
        int count = Math.min(idx.get(), totalRequests);
        long[] times = Arrays.copyOf(responseTimes, count);
        Arrays.sort(times);
        long sum = 0;
        for (long t : times) sum += t;
        double avg = (double) sum / count;
        long max = times[count - 1];
        long p95 = times[(int)(count * 0.95) - 1];
        long p99 = times[(int)(count * 0.99) - 1];
        long min = times[0];

        System.out.println("Total requests: " + totalRequests);
        System.out.println("Errors: " + errors.get());
        System.out.println("Total elapsed: " + totalElapsed + " ms");
        System.out.println("Avg response:  " + String.format("%.2f", avg) + " ms");
        System.out.println("Min response:  " + min + " ms");
        System.out.println("P95 response:  " + p95 + " ms");
        System.out.println("P99 response:  " + p99 + " ms");
        System.out.println("Max response:  " + max + " ms");
        System.out.println("Throughput:    " + String.format("%.1f", (double)totalRequests / totalElapsed * 1000) + " req/s");
        System.out.println("ASSERTION (avg < 3000ms): " + (avg < 3000 ? "PASS" : "FAIL") + " (" + String.format("%.2f", avg) + " ms)");
    }

    // --- HTTP Utilities ---

    static String login(String url, String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        conn.getOutputStream().flush();

        // Read response to consume it
        readResponse(conn);

        // Extract session cookie
        String setCookie = conn.getHeaderField("Set-Cookie");
        if (setCookie != null && setCookie.contains("JSESSIONID")) {
            return setCookie.split(";")[0];
        }
        // Try multiple Set-Cookie headers
        Map<String, List<String>> headers = conn.getHeaderFields();
        List<String> cookieHeaders = headers.get("Set-Cookie");
        if (cookieHeaders != null) {
            for (String c : cookieHeaders) {
                if (c.contains("JSESSIONID")) return c.split(";")[0];
            }
        }
        return setCookie != null ? setCookie.split(";")[0] : "";
    }

    static String httpRequest(String method, String url, String body, String cookie) throws Exception {
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        if (cookie != null) conn.setRequestProperty("Cookie", cookie);
        conn.setInstanceFollowRedirects(false);

        if (body != null && (method.equals("POST") || method.equals("PUT"))) {
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();
        }

        return readResponse(conn);
    }

    static String readResponse(HttpURLConnection conn) throws Exception {
        InputStream is;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            is = conn.getErrorStream();
        }
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }

    static long parseFirstId(String json) {
        // Simple parse: find "id": followed by number
        int idx = json.indexOf("\"id\":");
        if (idx < 0) return 1; // fallback
        int start = idx + 5;
        while (start < json.length() && !Character.isDigit(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return Long.parseLong(json.substring(start, end));
    }

    static int parseStock(String json) {
        int idx = json.indexOf("\"stock\":");
        if (idx < 0) return -1;
        int start = idx + 8;
        while (start < json.length() && !Character.isDigit(json.charAt(start)) && json.charAt(start) != '-') start++;
        int end = start;
        if (json.charAt(end) == '-') end++;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return Integer.parseInt(json.substring(start, end));
    }
}
