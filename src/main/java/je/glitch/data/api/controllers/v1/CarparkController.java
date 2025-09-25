package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.cache.RedisCache;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.ApiResponse;
import je.glitch.data.api.models.Carpark;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.models.ExtendedLiveParkingSpace;
import je.glitch.data.api.models.LiveParkingSpace;
import je.glitch.data.api.utils.ErrorType;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class CarparkController {
    private final MySQLConnection connection;
    private final RedisCache cache;

    public void handleGetCarparks(Context ctx) {
        List<Carpark> carparks = connection.getCarparkTable().getCarparks();
        ctx.json(new ApiResponse<>(carparks));
    }

    public void handleGetCarpark(Context ctx) {
        String idOrCode = ctx.pathParam("idOrCode");
        Carpark carpark;

        try {
            UUID.fromString(idOrCode);
            carpark = connection.getCarparkTable().getCarparkById(idOrCode);
        } catch (IllegalArgumentException ex) {
            carpark = connection.getCarparkTable().getCarparkByLiveTrackingCode(idOrCode);
        }

        if (carpark == null) {
            ctx.status(404).json(new ErrorResponse(ErrorType.NOT_FOUND, "Carpark not found"));
            return;
        }

        ctx.json(new ApiResponse<>(carpark));
    }

    public void handleGetLiveSpaces(Context ctx) {
        String date = ctx.queryParam("date");
        String includeCarparkInfo = ctx.queryParam("includeCarparkInfo");

        if (date == null) {
            List<Map<String, Object>> spaces = cache.getLiveParkingSpaces()
                .stream()
                .map(space -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", space.getName());
                    map.put("code", space.getCode());
                    map.put("spaces", space.getSpaces());
                    map.put("status", space.getStatus());
                    map.put("open", space.isOpen());
                    return map;
                })
                .toList();

            if (includeCarparkInfo != null && includeCarparkInfo.equalsIgnoreCase("true")) {
                List<Map<String, Object>> extendedSpaces = spaces.stream()
                        .map(space -> {
                            Carpark carparkInfo = connection.getCarparkTable()
                                    .getCarparkByLiveTrackingCode(space.get("code").toString());

                            Map<String, Object> map = new HashMap<>();
                            map.put("name", space.get("name").toString());
                            map.put("code", space.get("code").toString());
                            map.put("spaces", Integer.valueOf(space.get("spaces").toString()));
                            map.put("status", space.get("status").toString());
                            map.put("open", Boolean.valueOf(space.get("open").toString()));
                            map.put("carparkInfo", carparkInfo);
                            return map;
                        })
                        .toList();

                ctx.json(new ApiResponse<>(extendedSpaces));
                return;
            }

            ctx.json(new ApiResponse<>(spaces));
            return;
        }

        List<LiveParkingSpace> spaces = connection.getCarparkTable().getLiveSpacesForDate(date);
        ctx.json(new ApiResponse<>(spaces));
    }

    public void handleGetLiveSpacesDates(Context ctx) {
        List<String> dates = connection.getCarparkTable().getLiveSpacesDates();
        ctx.json(new ApiResponse<>(dates));
    }
}
