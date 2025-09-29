package je.glitch.data.api;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import je.glitch.data.api.cache.RedisCache;
import je.glitch.data.api.controllers.AuthController;
import je.glitch.data.api.controllers.MeController;
import je.glitch.data.api.controllers.admin.AdminStatsController;
import je.glitch.data.api.controllers.admin.AdminTokensController;
import je.glitch.data.api.controllers.admin.AdminUsersController;
import je.glitch.data.api.controllers.v1.*;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.Session;
import je.glitch.data.api.models.User;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.utils.ErrorType;
import je.glitch.data.api.utils.HttpException;
import je.glitch.data.api.utils.Utils;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.FileSessionDataStore;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .serializeNulls()
            // Temporary hack
            .addSerializationExclusionStrategy(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getName().equals("password");
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();

    private final MySQLConnection connection;
    private final CarparkController carparkController;
    private final VehicleController vehicleController;
    private final SimpleEndpointController simpleEndpointController;
    private final BusController busController;
    private final ErrorController errorController;

    private final AdminUsersController adminUsersController;
    private final AdminTokensController adminTokensController;
    private final AdminStatsController adminStatsController;

    private final AuthController authController;
    private final MeController meController;

    private final RedisCache cache;

    private final ExecutorService trackingThreadPool = Executors.newFixedThreadPool(4);

    public Server() {
        this.connection = new MySQLConnection();
        this.cache = new RedisCache();
        this.carparkController = new CarparkController(connection, cache);
        this.vehicleController = new VehicleController(connection);
        this.busController = new BusController(connection);
        this.simpleEndpointController = new SimpleEndpointController(connection, cache);
        this.errorController = new ErrorController();
        this.adminUsersController = new AdminUsersController(connection);
        this.adminTokensController = new AdminTokensController(connection);
        this.adminStatsController = new AdminStatsController(connection);
        this.authController = new AuthController(connection);
        this.meController = new MeController(connection);
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
            config.router.ignoreTrailingSlashes = true;
            config.showJavalinBanner = false;
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.allowHost("http://localhost:3000", "http://127.0.0.1:3000", "https://data.glitch.je", "https://opendata.je");
                    it.allowCredentials = true;
                });
            });
            config.jetty.modifyServletContextHandler(handler -> handler.setSessionHandler(fileSessionHandler()));
        }).start(8080);

        app.before(ctx -> {
            String path = ctx.path();

            // Let CORS plugin handle it
            if (ctx.method().name().equalsIgnoreCase("OPTIONS")) {
                return;
            }

            if (path.startsWith("/admin")) {
                Session session = ctx.sessionAttribute("session");

                if (session != null) {
                    User user = connection.getUserTable().getUser(session.getUserId());

                    if (user != null && user.isSiteAdmin()) {
                        return;
                    }
                }
                throw new HttpException(ErrorType.FORBIDDEN, 403, "You must be an administrator to access this endpoint");
            }
        });

        app.after(ctx -> {
            String path = ctx.path();

            // We only care about public API endpoints
            if (!path.startsWith("/v1")) {
                return;
            }

            String method = ctx.method().name();
            int status = ctx.statusCode();
            String ip = ctx.ip();
            String userAgent = ctx.userAgent();
            String tokenHeader = ctx.header("Authorization");

            trackingThreadPool.submit(() -> {
                String apiTokenId = null;
                if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                    String token = tokenHeader.substring(7);
                    apiTokenId = connection.getApiKeyTable().getIdFromKey(token);
                }
                connection.getLogTable().trackRequest(method, path, status, ip, userAgent, apiTokenId);
            });
        });

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
        app.get("/v1/charts/registered-vehicles", simpleEndpointController::handleGetRegisteredVehiclesChart);

        app.get("/v1/bus/stops", busController::handleGetStops);

        app.get("/admin/users", adminUsersController::handleGetUsers);
        app.post("/admin/users/new", adminUsersController::handleCreateUser);
        app.post("/admin/users/{userId}", adminUsersController::handleUpdateUser);
        app.delete("/admin/users/{userId}", adminUsersController::handleDeleteUser);
        app.get("/admin/users/{userId}/tokens", adminTokensController::handleListTokensForUser);
        app.get("/admin/tokens", adminTokensController::handleListTokens);
        app.post("/admin/tokens/new", adminTokensController::handleCreateToken);
        app.delete("/admin/tokens/{tokenId}", adminTokensController::handleDeleteToken);
        app.get("/admin/stats", adminStatsController::handleGetStats);
        app.get("/admin/stats/daily-requests", adminStatsController::handleGetDailyRequestsChart);

        app.post("/auth/login", authController::handleLogin);
        app.post("/auth/register", authController::handleRegister);

        app.get("/me/session", meController::handleGetSession);

        app.exception(Exception.class, errorController::handleException);
        app.error(404, errorController::handleNotFound);
    }

    public static SessionHandler fileSessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
        SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
        sessionCache.setSessionDataStore(fileSessionDataStore());
        sessionHandler.setSessionCache(sessionCache);
        sessionHandler.setSecureRequestOnly(false);
        sessionHandler.setSameSite(HttpCookie.SameSite.NONE);
        sessionHandler.setHttpOnly(true);
        // make additional changes to your SessionHandler here
        return sessionHandler;
    }

    private static FileSessionDataStore fileSessionDataStore() {
        FileSessionDataStore fileSessionDataStore = new FileSessionDataStore();
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File storeDir = new File(baseDir, "javalin-session-store");
        storeDir.mkdir();
        fileSessionDataStore.setStoreDir(storeDir);
        return fileSessionDataStore;
    }
}
