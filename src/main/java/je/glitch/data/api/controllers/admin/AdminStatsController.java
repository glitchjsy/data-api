package je.glitch.data.api.controllers.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.ApiRequestStats;
import je.glitch.data.api.models.ApiResponse;
import je.glitch.data.api.models.DailyRequestStat;
import je.glitch.data.api.models.EndpointRequestStat;
import je.glitch.data.api.utils.Utils;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AdminStatsController {
    private final MySQLConnection connection;

    public void handleGetStats(Context ctx) {
        ApiRequestStats stats = connection.getLogTable().getRequestStats();

        JsonObject object = new JsonObject();
        object.addProperty("apiUptime", Utils.getUptimeString());
        object.addProperty("totalAllTime", stats.getTotalAllTime());
        object.addProperty("total24Hours", stats.getTotal24Hours());
        object.addProperty("total7Days", stats.getTotal7Days());
        object.addProperty("total30Days", stats.getTotal30Days());

        ctx.json(new ApiResponse<>(object));
    }

    public void handleGetDailyRequestsChart(Context ctx) {
        int year = ctx.queryParamAsClass("year", Integer.class).getOrDefault(LocalDate.now().getYear());
        int month = ctx.queryParamAsClass("month", Integer.class).getOrDefault(LocalDate.now().getMonthValue());

        List<DailyRequestStat> stats = connection.getLogTable().getDailyStatsForMonth(year, month);

        Map<String, JsonObject> dayMap = new LinkedHashMap<>();

        for (DailyRequestStat stat : stats) {
            JsonObject dayObj = dayMap.computeIfAbsent(stat.getDay(), d -> {
                JsonObject obj = new JsonObject();
                obj.addProperty("day", d);
                obj.addProperty("authenticated", 0);
                obj.addProperty("unauthenticated", 0);
                return obj;
            });

            if ("authenticated".equals(stat.getAuthStatus())) {
                dayObj.addProperty("authenticated", dayObj.get("authenticated").getAsLong() + stat.getTotal());
            } else {
                dayObj.addProperty("unauthenticated", dayObj.get("unauthenticated").getAsLong() + stat.getTotal());
            }
        }

        JsonArray array = new JsonArray();
        for (JsonObject obj : dayMap.values()) {
            obj.addProperty(
                    "total",
                    obj.get("authenticated").getAsLong() + obj.get("unauthenticated").getAsLong()
            );
            array.add(obj);
        }

        ctx.json(new ApiResponse<>(array));
    }


    public void handleGetTopEndpoints(Context ctx) {
        Integer year = ctx.queryParamAsClass("year", Integer.class).getOrDefault(null);
        Integer month = ctx.queryParamAsClass("month", Integer.class).getOrDefault(null);

        List<EndpointRequestStat> stats = connection.getLogTable().getTopEndpoints(year, month);

        JsonArray array = new JsonArray();
        for (EndpointRequestStat stat : stats) {
            JsonObject obj = new JsonObject();
            obj.addProperty("path", stat.getPath());
            obj.addProperty("total", stat.getTotal());
            array.add(obj);
        }

        ctx.json(new ApiResponse<>(array));
    }

}
