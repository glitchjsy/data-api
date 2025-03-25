package je.glitch.data.api.controllers.v1;

import com.google.gson.JsonElement;
import io.javalin.http.Context;
import je.glitch.data.api.cache.RedisCache;
import je.glitch.data.api.database.MySQLConnection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleEndpointController {
    private final MySQLConnection connection;
    private final RedisCache cache;

    public void handleGetEatsafe(Context ctx) {
        ctx.json(cache.getEatSafeData());
    }

    public void handleGetToilets(Context ctx) {
        ctx.json(cache.getToiletData());
    }

    public void handleGetRecycling(Context ctx) {
        ctx.json(cache.getRecyclingData());
    }

    public void handleGetDefibrillators(Context ctx) {
        ctx.json(cache.getDefibrillatorData());
    }

    public void handleGetBusPassengersChart(Context ctx) {
        ctx.json(cache.getBusPassengersChartData());
    }

    public void handleGetRoadTrafficChart(Context ctx) {
        ctx.json(cache.getRoadTrafficChartData());
    }

    public void handleGetDrivingResultsChart(Context ctx) {
        ctx.json(cache.getDrivingTestResultsChartData());
    }
}
