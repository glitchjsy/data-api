package je.glitch.data.api.controllers.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.modelsnew.entities.ApiRequestStatsEntity;
import je.glitch.data.api.models.ApiResponse;
import je.glitch.data.api.modelsnew.entities.DailyRequestStatEntity;
import je.glitch.data.api.modelsnew.entities.EndpointRequestStatEntity;
import je.glitch.data.api.modelsnew.outbound.admin.AdminRequestStatsResponse;
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
        ApiRequestStatsEntity stats = connection.getLogTable().getRequestStats();

        ctx.json(new ApiResponse<>(new AdminRequestStatsResponse(
                Utils.getUptimeString(),
                stats.getTotalAllTime(),
                stats.getTotal24Hours(),
                stats.getTotal7Days(),
                stats.getTotal30Days()
        )));
    }

    public void handleGetDailyRequestsChart(Context ctx) {
        int year = ctx.queryParamAsClass("year", Integer.class).getOrDefault(LocalDate.now().getYear());
        int month = ctx.queryParamAsClass("month", Integer.class).getOrDefault(LocalDate.now().getMonthValue());

        List<DailyRequestStatEntity> stats = connection.getLogTable().getDailyStatsForMonth(year, month);

        Map<String, JsonObject> dayMap = new LinkedHashMap<>();

        for (DailyRequestStatEntity stat : stats) {
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

        List<EndpointRequestStatEntity> stats = connection.getLogTable().getTopEndpoints(year, month);

        JsonArray array = new JsonArray();
        for (EndpointRequestStatEntity stat : stats) {
            JsonObject obj = new JsonObject();
            obj.addProperty("path", stat.getPath());
            obj.addProperty("total", stat.getTotal());
            array.add(obj);
        }

        ctx.json(new ApiResponse<>(array));
    }

}
