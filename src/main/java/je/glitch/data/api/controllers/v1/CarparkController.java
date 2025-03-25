package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.cache.RedisCache;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.Carpark;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.models.ExtendedLiveParkingSpace;
import je.glitch.data.api.models.LiveParkingSpace;
import je.glitch.data.api.utils.ErrorType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CarparkController {
    private final MySQLConnection connection;
    private final RedisCache cache;

    public void handleGetCarparks(Context ctx) {
        List<Carpark> carparks = connection.getCarparkTable().getCarparks();
        ctx.json(carparks);
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

        ctx.json(carpark);
    }

    public void handleGetLiveSpaces(Context ctx) {
        String date = ctx.queryParam("date");
        String includeCarparkInfo = ctx.queryParam("includeCarparkInfo");

        if (date == null) {
            List<LiveParkingSpace> spaces = cache.getLiveParkingSpaces();

            if (includeCarparkInfo != null && includeCarparkInfo.equalsIgnoreCase("true")) {
                List<ExtendedLiveParkingSpace> extendedSpaces = spaces.stream()
                        .map(space -> {
                            Carpark carparkInfo = connection.getCarparkTable().getCarparkByLiveTrackingCode(space.getCode());
                            return new ExtendedLiveParkingSpace(
                                    space.getId(),
                                    space.getCreatedAt(),
                                    space.getName(),
                                    space.getCode(),
                                    space.getSpaces(),
                                    space.getStatus(),
                                    space.isOpen(),
                                    carparkInfo
                            );
                        })
                        .toList();
                ctx.json(extendedSpaces);
                return;
            }

            ctx.json(spaces);
            return;
        }

        List<LiveParkingSpace> spaces = connection.getCarparkTable().getLiveSpacesForDate(date);
        ctx.json(spaces);
    }

    public void handleGetLiveSpacesDates(Context ctx) {
        List<String> dates = connection.getCarparkTable().getLiveSpacesDates();
        ctx.json(dates);
    }
}
