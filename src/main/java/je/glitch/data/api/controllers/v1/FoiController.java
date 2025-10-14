package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.ApiResponse;
import je.glitch.data.api.models.FoiRequest;
import je.glitch.data.api.utils.ErrorType;
import je.glitch.data.api.utils.HttpException;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class FoiController {
    private final MySQLConnection connection;

    public void handleGetFoiRequests(Context ctx) {
        connection.getFoiTable().getRequests(ctx);
    }

    public void handleGetById(Context ctx) {
        String id = ctx.pathParam("id");

        try {
            FoiRequest request = connection.getFoiTable().getById(Integer.parseInt(id));
            ctx.json(new ApiResponse<>(request));
        } catch (NumberFormatException ex) {
            throw new HttpException(ErrorType.INVALID_REQUEST, 400, "The id provided is invalid");
        }
    }

    public void handleGetStats(Context ctx) throws SQLException {
        String startDate = ctx.queryParam("startDate");
        String endDate = ctx.queryParam("endDate");

        Map<String, Object> stats = connection.getFoiTable().getStats(startDate, endDate);
        ctx.json(new ApiResponse<>(stats));
    }

    public void handleGetAuthors(Context ctx) {
        List<String> authors = connection.getFoiTable().getAllDistinctAuthors();
        ctx.json(new ApiResponse<>(authors));
    }

    public void handleGetProducers(Context ctx) {
        List<String> producers = connection.getFoiTable().getAllDistinctProducers();
        ctx.json(new ApiResponse<>(producers));
    }
}
