package je.glitch.data.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import je.glitch.data.api.cache.RedisCache;
import je.glitch.data.api.controllers.v1.*;
import je.glitch.data.api.database.MySQLConnection;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class Server {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    private final MySQLConnection connection;
    private final CarparkController carparkController;
    private final VehicleController vehicleController;
    private final SimpleEndpointController simpleEndpointController;
    private final BusController busController;
    private final ErrorController errorController;

    private final RedisCache cache;

    public Server() {
        this.connection = new MySQLConnection();
        this.cache = new RedisCache();
        this.carparkController = new CarparkController(connection, cache);
        this.vehicleController = new VehicleController(connection);
        this.busController = new BusController(connection);
        this.simpleEndpointController = new SimpleEndpointController(connection, cache);
        this.errorController = new ErrorController();
    }

    public static void main(String[] args) {
        new Server().startup();
    }

    private void startup() {
        JsonMapper gsonMapper = new JsonMapper() {
            @Override
            public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                return GSON.toJson(obj, type);
            }

            @Override
            public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                return GSON.fromJson(json, targetType);
            }
        };

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(gsonMapper);
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start(8080);

        app.head("/health", ctx -> ctx.status(200));
        app.head("/health/fetcher", simpleEndpointController::handleGetFetcherHeartbeat);

        app.get("/v1/carparks", carparkController::handleGetCarparks);
        app.get("/v1/carparks/spaces", carparkController::handleGetLiveSpaces);
        app.get("/v1/carparks/spaces/dates", carparkController::handleGetLiveSpacesDates);
        app.get("/v1/carparks/{idOrCode}", carparkController::handleGetCarpark);

        app.get("/v1/vehicles", vehicleController::handleGetVehicles);
        app.get("/v1/vehicles/stats", vehicleController::handleGetStats);
        app.get("/v1/vehicles/colors", vehicleController::handleGetColors);
        app.get("/v1/vehicles/makes", vehicleController::handleGetMakes);
        app.get("/v1/vehicles/models", vehicleController::handleGetModels);
        app.get("/v1/vehicles/lookup/{plate}", vehicleController::handleGetPlate);

        app.get("/v1/eatsafe", simpleEndpointController::handleGetEatsafe);
        app.get("/v1/toilets", simpleEndpointController::handleGetToilets);
        app.get("/v1/recycling", simpleEndpointController::handleGetRecycling);
        app.get("/v1/defibrillators", simpleEndpointController::handleGetDefibrillators);
        // TODO: Product recalls

        app.get("/v1/charts/bus-passengers", simpleEndpointController::handleGetBusPassengersChart);
        app.get("/v1/charts/road-traffic", simpleEndpointController::handleGetRoadTrafficChart);
        app.get("/v1/charts/driving-test-results", simpleEndpointController::handleGetDrivingResultsChart);
        app.get("/v1/charts/monthly-rainfall", simpleEndpointController::handleGetMonthlyRainfallChart);

        app.get("/v1/bus/stops", busController::handleGetStops);

        app.exception(Exception.class, errorController::handleException);
        app.error(404, errorController::handleNotFound);
        app.error(500, errorController::handleServerError);
    }
}
