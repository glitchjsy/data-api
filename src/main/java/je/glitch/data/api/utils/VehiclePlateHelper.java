package je.glitch.data.api.utils;

import com.google.gson.JsonObject;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.enums.VehicleFuelType;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VehiclePlateHelper {
    private static final String GOV_URL = "https://vehicle-search.gov.je";
    private static final String SEARCH_URL = GOV_URL + "/search";

    private static final Map<String, VehicleFuelType> FUEL_TYPE_MAP = Map.of(
            "Petrol", VehicleFuelType.PETROL,
            "Heavy Oil", VehicleFuelType.HEAVY_OIL,
            "Electric", VehicleFuelType.ELECTRIC,
            "Hybrid Electric", VehicleFuelType.HYBRID_ELECTRIC,
            "Gas", VehicleFuelType.GAS,
            "Diesel Electric", VehicleFuelType.DIESEL_ELECTRIC,
            "Gas Bi Fuel", VehicleFuelType.GAS_BI_FUEL,
            "Steam", VehicleFuelType.STEAM,
            "Unknown", VehicleFuelType.UNKNOWN
    );

    private static Integer unknownToNull(String value) {
        if (value == null) return null;
        return value.equalsIgnoreCase("Not known") ? null : Utils.parseInteger(value);
    }

    /**
     * Parse the vehicle information from the html.
     *
     * @param plate The plate for search for
     * @return The vehicle info
     */
    public static JsonObject parseVehicleInfo(String plate, MySQLConnection connection) throws Exception {
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

        Map<String, String> temp = vehicleDataList.stream()
                .collect(Collectors.toMap(d -> d.getKey().toLowerCase(), VehicleData::getValue, (a,b)->b));

        JsonObject json = new JsonObject();

        json.addProperty("make", temp.get("make"));
        json.addProperty("type", temp.get("type"));
        json.addProperty("color", temp.get("colour"));
        json.addProperty("cylinderCapacity", unknownToNull(temp.get("cylinder capacity").replaceAll("\\s*\\(.*\\)", "").trim()));
        json.addProperty("weight", unknownToNull(temp.get("weight")));
        json.addProperty("co2Emissions", unknownToNull(temp.get("co₂ emissions")));
        json.addProperty("fuelType", FUEL_TYPE_MAP.getOrDefault(temp.get("fuel type"), VehicleFuelType.UNKNOWN).name());
        json.addProperty("firstRegisteredAt", Utils.parseDateToISO(temp.get("date of first registration")));
        json.addProperty("firstRegisteredInJerseyAt", Utils.parseDateToISO(temp.get("date registered in jersey")));

        String ownersText = temp.get("number of previous owners"); // e.g., "8 Owners (incl. 5 Traders)"
        int previousOwners = 0;
        int previousTraders = 0;

        if (ownersText != null) {
            // Extract the first number for owners
            Matcher mOwners = Pattern.compile("(\\d+)").matcher(ownersText);
            if (mOwners.find()) {
                previousOwners = Integer.parseInt(mOwners.group(1));
            }

            // Extract the number of traders from "(incl. X Traders)"
            Matcher mTraders = Pattern.compile("incl\\.\\s*(\\d+)\\s*Traders").matcher(ownersText);
            if (mTraders.find()) {
                previousTraders = Integer.parseInt(mTraders.group(1));
            }
        }

        json.addProperty("previousOwners", previousOwners);
        json.addProperty("previousTraders", previousTraders);

        return json;
    }

    /**
     * Fetch vehicle information from the Government vehicle search page.
     * Called from {@link #parseVehicleInfo(String, MySQLConnection)}
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
