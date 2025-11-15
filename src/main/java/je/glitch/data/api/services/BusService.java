package je.glitch.data.api.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.BusStop;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class BusService {
    private final MySQLConnection connection;

    private final Cache<String, List<BusStop>> busStopsCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1)
            .build();

    public List<BusStop> getBusStops() {
        return busStopsCache.get("stops", k -> connection.getBusTable().getBusStops());
    }
}
