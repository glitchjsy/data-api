package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.models.*;
import je.glitch.data.api.services.CarparkService;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.utils.ErrorType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CarparkController {

    private final CarparkService service;

    public void handleGetCarparks(Context ctx) {
        ctx.json(new ApiResponse<>(service.getAllCarparks()));
    }

    public void handleGetCarpark(Context ctx) {
        String idOrCode = ctx.pathParam("idOrCode");
        var carpark = service.getCarparkByIdOrCode(idOrCode);

        if (carpark == null) {
            ctx.status(404).json(new ErrorResponse(ErrorType.NOT_FOUND, "Carpark not found"));
            return;
        }

        ctx.json(new ApiResponse<>(carpark));
    }

    public void handleGetLiveSpaces(Context ctx) {
        String date = ctx.queryParam("date");
        boolean includeCarparkInfo = "true".equalsIgnoreCase(ctx.queryParam("includeCarparkInfo"));

        if (date == null) {
            ctx.json(new ApiResponse<>(service.getLiveSpaces(includeCarparkInfo)));
        } else {
            ctx.json(new ApiResponse<>(service.getLiveSpacesForDate(date)));
        }
    }

    public void handleGetLiveSpacesDates(Context ctx) {
        ctx.json(new ApiResponse<>(service.getLiveSpacesDates()));
    }

    public void handleGetParkingStats(Context ctx) {
        ctx.json(new ApiResponse<>(service.getParkingStats()));
    }
}
