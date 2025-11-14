package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.models.ApiResponse;
import je.glitch.data.api.services.BusService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BusController {
    private final BusService service;

    public void handleGetStops(Context ctx) {
        ctx.json(new ApiResponse<>(service.getBusStops()));
    }
}
