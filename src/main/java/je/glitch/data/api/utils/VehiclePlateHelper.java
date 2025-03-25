package je.glitch.data.api.utils;

import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VehiclePlateHelper {
    private static final String GOV_URL = "https://vehicle-search.gov.je";
    private static final String SEARCH_URL = GOV_URL + "/search";

    /**
     * Parse the vehicle information from the html.
     *
     * @param plate The plate for search for
     * @return A list of objects containg the vehicle info
     */
    public static List<VehicleData> parseVehicleInfo(String plate) throws IOException, NoSuchAlgorithmException, InterruptedException, KeyManagementException {
        Document document = Jsoup.parse(fetchVehicleInfo(plate));
        List<Element> rows = document.getElementsByClass("detail-row");

        if (rows.isEmpty()) {
            return null;
        }

        List<VehicleData> vehicleDataList = new ArrayList<>();

        for (Element row : rows) {
            List<Element> cells = row.getElementsByTag("td");

            if (cells.size() >= 2) {
                String key = cells.get(0).text();
                String value = cells.get(1).text();
                vehicleDataList.add(new VehicleData(key, value));
            }
        }

        return vehicleDataList;
    }

    /**
     * Fetch vehicle information from the Government vehicle search page.
     * Called from {@link #parseVehicleInfo(String)}
     *
     * @param plate The plate to search for
     * @return The html content of the page
     */
    private static String fetchVehicleInfo(String plate) throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {
        // Ignore SSL validation (TODO BAD: REMOVE THIS)
        TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());


        HttpClient client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();

        HttpResponse<String> getResponse = Utils.sendRequest(client, GOV_URL, "GET", null);
        String formHtml = getResponse.body();
        String cookies = getResponse.headers().allValues("set-cookie").stream()
                .map(cookie -> cookie.split(";")[0])
                .collect(Collectors.joining("; "));

        // Extract CSRF token
        Element csrfToken = Jsoup.parse(formHtml).selectFirst("input[name=_csrf]");
        if (csrfToken == null) throw new IllegalStateException("CSRF token not found!");

        String formData = String.format("_csrf=%s&plate=%s",
                URLEncoder.encode(csrfToken.val(), StandardCharsets.UTF_8),
                URLEncoder.encode(plate, StandardCharsets.UTF_8));

        HttpResponse<String> postResponse = Utils.sendRequest(client, SEARCH_URL, "POST", formData, cookies);
        return postResponse.body();
    }

    @Data
    public static class VehicleData {
        private final String key;
        private final String value;
    }
}
