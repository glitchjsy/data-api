package je.glitch.data.api.cache;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import je.glitch.data.api.Server;
import je.glitch.data.api.models.BusStop;
import je.glitch.data.api.models.LiveParkingSpace;
import je.glitch.data.api.models.PublicToilet;
import je.glitch.data.api.models.RecyclingCentre;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

public class RedisCache {
    private final JedisPool pool;

    public RedisCache() {
        this.pool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
    }

    public List<LiveParkingSpace> getLiveParkingSpaces() {
        List<LiveParkingSpace> parkingSpaces = new ArrayList<>();

        try (Jedis jedis = pool.getResource()) {
            String rawData = jedis.get("data-livespaces:json");
            if (rawData != null) {
                JsonObject data = Server.GSON.fromJson(rawData, JsonObject.class);
                JsonArray results = data.getAsJsonArray("results");

                for (JsonElement element : results) {
                    JsonObject obj = element.getAsJsonObject();
                    LiveParkingSpace space = new LiveParkingSpace(
                            null,
                            null,
                            obj.get("name").getAsString(),
                            obj.get("code").getAsString(),
                            obj.get("spaces").getAsInt(),
                            obj.get("status").getAsString(),
                            obj.get("open").getAsBoolean()
                    );
                    parkingSpaces.add(space);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return parkingSpaces;
    }

    public JsonElement getEatSafeData() {
        return getBasicData("data-eatsafe:json");
    }

    public JsonElement getToiletData() {
        return getBasicData("data-toilets:json");
    }

    public JsonElement getRecyclingData() {
        return getBasicData("data-recycling:json");
    }

    public JsonElement getDefibrillatorData() {
        return getBasicData("data-defibrillators:json");
    }

    public JsonElement getBusPassengersChartData() {
        return getBasicData("data-bus-passengers:json");
    }

    public JsonElement getRoadTrafficChartData() {
        return getBasicData("data-road-traffic:json");
    }

    public JsonElement getDrivingTestResultsChartData() {
        return getBasicData("data-driving-test-results:json");
    }

    public boolean checkFetcherHeartbeat() {
        try (Jedis jedis = pool.getResource()) {
            String redisData = jedis.get("data-fetcher-heartbeat");
            long keyTimestamp = Long.parseLong(redisData);
            long currentTime = System.currentTimeMillis();

            // Check if time in redis is more than 5 minutes ago
            if (currentTime - keyTimestamp > 5 * 60 * 1000) {
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private JsonElement getBasicData(String key) {
        try (Jedis jedis = pool.getResource()) {
            String rawData = jedis.get(key);
            if (rawData != null) {
                return Server.GSON.fromJson(rawData, JsonElement.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
