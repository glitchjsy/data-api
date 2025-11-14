package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.models.ApiResponse;
import je.glitch.data.api.services.CourtService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CourtController {
    private final CourtService service;

    public void handleGetMagistratesHearings(Context ctx) {
        service.getMagistratesHearings(ctx);
    }

    public void handleGetMagistratesResults(Context ctx) {
        service.getMagistratesResults(ctx);
    }

    public void handleGetDistinctMagistratesHearingFields(Context ctx) {
        ctx.json(new ApiResponse<>(service.getDistinctMagistratesHearingFields()));
    }

    public void handleGetDistinctMagistratesResultFields(Context ctx) {
        ctx.json(new ApiResponse<>(service.getDistinctMagistratesResultFields()));
    }
}
