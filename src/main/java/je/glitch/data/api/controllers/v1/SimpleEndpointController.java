package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.cache.RedisCache;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.ApiResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleEndpointController {
    private final MySQLConnection connection;
    private final RedisCache cache;

    public void handleGetEatsafe(Context ctx) {
        ctx.json(new ApiResponse<>(cache.getEatSafeData()));
    }

    public void handleGetToilets(Context ctx) {
        ctx.json(new ApiResponse<>(cache.getToiletData()));
    }

    public void handleGetRecycling(Context ctx) {
        ctx.json(new ApiResponse<>(cache.getRecyclingData()));
    }

    public void handleGetDefibrillators(Context ctx) {
        ctx.json(new ApiResponse<>(cache.getDefibrillatorData()));
    }

    public void handleGetBusPassengersChart(Context ctx) {
        ctx.json(new ApiResponse<>(cache.getBusPassengersChartData()));
    }

    public void handleGetRoadTrafficChart(Context ctx) {
        ctx.json(new ApiResponse<>(cache.getRoadTrafficChartData()));
    }

    public void handleGetDrivingResultsChart(Context ctx) {
        ctx.json(new ApiResponse<>(cache.getDrivingTestResultsChartData()));
    }

    public void handleGetMonthlyRainfallChart(Context ctx) {
        ctx.json(new ApiResponse<>(cache.getMonthlyRainfallData()));
    }

    public void handleGetRegisteredVehiclesChart(Context ctx) {
        ctx.json(new ApiResponse<>(cache.getRegisteredVehiclesChartData()));
    }

    public void handleGetFetcherHeartbeat(Context ctx) {
        boolean alive = cache.checkFetcherHeartbeat();
        ctx.status(alive ? 200 : 503);
    }
}
