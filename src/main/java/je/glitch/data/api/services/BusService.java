package je.glitch.data.api.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import je.glitch.data.api.Server;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.modelsnew.entities.BusStopEntity;
import je.glitch.data.api.modelsnew.inbound.BusLiveUpdateInbound;
import je.glitch.data.api.modelsnew.inbound.BusRouteInbound;
import je.glitch.data.api.modelsnew.inbound.BusStopLiveInbound;
import je.glitch.data.api.modelsnew.outbound.bus.*;
import je.glitch.data.api.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class BusService {
    private final MySQLConnection connection;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String API_BASE_URL = "http://sojbuslivetimespublic.azurewebsites.net";
    private static final String API_GET_ROUTES_URL = API_BASE_URL + "/api/Values/v1/GetRoutes";
    private static final String API_STOP_URL = API_BASE_URL + "/api/Values/v1/BusStop/";
    private static final String API_UPDATES_URL = API_BASE_URL + "/api/Values/v1/";
    private static final String API_MIN_UPDATES_URL = API_BASE_URL + "/api/Values/v1/GetMin/";

    private final Cache<String, List<BusStopResponse>> busStopsStaticCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1)
            .build();

    private final Cache<String, List<BusRouteResponse>> routeCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(1)
            .build();

    private final Cache<Integer, List<BusStopLiveDepartureResponse>> stopLiveCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(50)
            .build();

    private final Cache<String, List<BusLiveUpdateResponse>> liveUpdatesCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .maximumSize(100)
            .build();

    private final Cache<String, List<BusLiveUpdateMinResponse>> minUpdatesCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .maximumSize(100)
            .build();

    public BusStopResponse getBusStop(int stopNumber) {
        BusStopEntity entity = connection.getBusTable().getBusStopByStopNumber(String.valueOf(stopNumber));
        if (entity == null) {
            return null;
        }
        return new BusStopResponse(
                entity.getId(),
                entity.getName(),
                entity.getStopNumber(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.isShelter()
        );
    }

    public List<BusStopResponse> getBusStops() {
        return busStopsStaticCache.get("stops", k -> connection.getBusTable().getBusStops()
                .stream()
                .map(e -> new BusStopResponse(
                        e.getId(),
                        e.getName(),
                        e.getStopNumber(),
                        e.getLatitude(),
                        e.getLongitude(),
                        e.isShelter()
                ))
                .toList());
    }

    public List<BusRouteResponse> getBusRoutes() {
        try {
            HttpResponse<String> res = Utils.sendRequest(httpClient, API_GET_ROUTES_URL, "GET", null);

            if (res.statusCode() != 200 || res.body() == null || res.body().isBlank()) {
                log.error("Bus API returned bad status: " + res.statusCode());
                return new ArrayList<>();
            }

            JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
            JsonArray results = root.getAsJsonArray("routes");

            List<BusRouteInbound> inbound = Server.GSON.fromJson(results, Utils.listOf(BusRouteInbound.class));

            return inbound.stream()
                    .map(r -> new BusRouteResponse(r.getNumber(), r.getName(), r.getColor()))
                    .toList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<BusStopLiveDepartureResponse> getStopLiveDepartures(int stopId) {
        return stopLiveCache.get(stopId, key -> {
            try {
                HttpResponse<String> res = Utils.sendRequest(httpClient, API_STOP_URL + stopId, "GET", null);

                if (res.statusCode() != 200 || res.body() == null || res.body().isBlank()) {
                    log.error("Bus API returned bad status: " + res.statusCode());
                    return new ArrayList<>();
                }

                JsonArray array = JsonParser.parseString(res.body()).getAsJsonArray();
                List<BusStopLiveInbound.LiveDeparture> inbound = Server.GSON.fromJson(array, Utils.listOf(BusStopLiveInbound.LiveDeparture.class));

                return inbound.stream()
                        .map(r -> new BusStopLiveDepartureResponse(r.getDestination(), r.getEta(), r.getServiceNumber(), r.getStopNumber()))
                        .toList();
            } catch (Exception ex) {
                ex.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    public List<BusLiveUpdateResponse> getLiveUpdates(Integer secondsAgo, Double lat, Double lon, Integer radius, Integer limit) {
        String url = buildUpdatesUrl(API_UPDATES_URL, secondsAgo, lat, lon, radius, limit);

        try {
            HttpResponse<String> res = Utils.sendRequest(httpClient, url, "GET", null);

            JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
            JsonArray updates = root.getAsJsonArray("updates");

            List<BusLiveUpdateInbound.Update> inbound = Server.GSON.fromJson(updates, Utils.listOf(BusLiveUpdateInbound.Update.class));

            return inbound.stream()
                    .map(r -> new BusLiveUpdateResponse(
                            r.getDeviceId(), r.getAssetType(), r.getRegistration(), r.getServiceNumber(), r.getServiceName(),
                            r.getServiceOperator(), r.getOriginalStartTime(), r.getTimeOfUpdate(), r.getDirection(),
                            r.getLatitude(), r.getLongitude(), r.getBearing(), r.getSecondsAgo()
                    ))
                    .toList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<BusLiveUpdateMinResponse> getMinUpdates(Integer secondsAgo, Double lat, Double lon, Integer radius, Integer limit) {
        String url = buildUpdatesUrl(API_MIN_UPDATES_URL, secondsAgo, lat, lon, radius, limit);

        try {
            HttpResponse<String> res = Utils.sendRequest(httpClient, url, "GET", null);

            JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
            JsonArray updates = root.getAsJsonArray("minimumInfoUpdates");

            List<BusLiveUpdateInbound.MinUpdate> inbound =  Server.GSON.fromJson(updates, Utils.listOf(BusLiveUpdateInbound.MinUpdate.class));

            return inbound.stream()
                    .map(r -> new BusLiveUpdateMinResponse(
                           r.getBus(), r.getLine(), r.getCategory(), r.getLatitude(), r.getLongitude(), r.getBearing(), r.getDirection(), r.getTime(), r.getAge()
                    ))
                    .toList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String buildUpdatesUrl(String base, Integer secondsAgo, Double lat, Double lon, Integer radius, Integer limit) {
        StringBuilder url = new StringBuilder(base);

        if (secondsAgo != null) {
            url.append(secondsAgo);
        } else {
            return url.toString();
        }
        if (lat != null) {
            url.append("/").append(lat);
        } else {
            return url.toString();
        }
        if (lon != null) {
            url.append("/").append(lon);
        } else {
            return url.toString();
        }
        if (radius != null) {
            url.append("/").append(radius);
        } else {
            return url.toString();
        }
        if (limit != null) {
            url.append("/").append(limit);
        }
        return url.toString();
    }
}
