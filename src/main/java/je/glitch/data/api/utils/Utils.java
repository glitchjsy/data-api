package je.glitch.data.api.utils;

import lombok.Data;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern DATE_FORMAT = Pattern.compile("\\d{4}/\\d{2}/\\d{2}"); // Regex for YYYY/MM/DD format

    public static QueryDateResult queryDateSql(String dateField, String startDate, String endDate) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (startDate != null) {
            if (!DATE_FORMAT.matcher(startDate).matches()) {
                throw new IllegalArgumentException("Invalid start date. Please use the format YYYY/MM/DD.");
            }
            conditions.add(dateField + " >= ?");
            params.add(startDate);
        }

        if (endDate != null) {
            if (!DATE_FORMAT.matcher(endDate).matches()) {
                throw new IllegalArgumentException("Invalid end date. Please use the format YYYY/MM/DD.");
            }
            conditions.add(dateField + " <= ?");
            params.add(endDate);
        }

        String dateSql = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
        return new QueryDateResult(dateSql, params);
    }

    public static HttpResponse<String> sendRequest(HttpClient client, String url, String method, String body) throws IOException, InterruptedException {
        return sendRequest(client, url, method, body, null);
    }

    public static HttpResponse<String> sendRequest(HttpClient client, String url, String method, String body, String cookies) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");

        if (cookies != null) {
            requestBuilder.header("Cookie", cookies);
        }

        if (method.equalsIgnoreCase("POST") && body != null) {
            requestBuilder.header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
        } else {
            requestBuilder.GET();
        }

        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Data
    public static class QueryDateResult {
        private final String dateSql;
        private final List<Object> dateParams;
    }
}
