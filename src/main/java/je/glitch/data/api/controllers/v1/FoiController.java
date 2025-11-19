package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.models.ApiResponse;
import je.glitch.data.api.modelsnew.outbound.foi.FoiRequestResponse;
import je.glitch.data.api.services.FoiService;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class FoiController {
    private final FoiService service;

    public void handleGetFoiRequests(Context ctx) {
        service.getRequests(ctx);
    }

    public void handleGetById(Context ctx) {
        String id = ctx.pathParam("id");
        FoiRequestResponse request = service.getById(id);
        ctx.json(new ApiResponse<>(request));
    }

    public void handleGetStats(Context ctx) throws SQLException {
        String startDate = ctx.queryParam("startDate");
        String endDate = ctx.queryParam("endDate");

        Map<String, Object> stats = service.getStats(startDate, endDate);
        ctx.json(new ApiResponse<>(stats));
    }

    public void handleGetAuthors(Context ctx) {
        List<String> authors = service.getAuthors();
        ctx.json(new ApiResponse<>(authors));
    }

    public void handleGetProducers(Context ctx) {
        List<String> producers = service.getProducers();
        ctx.json(new ApiResponse<>(producers));
    }
}
