package je.glitch.data.api.controllers.v1;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.ApiResponse;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.utils.ErrorType;
import je.glitch.data.api.utils.VehiclePlateHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class VehicleController {
    private final MySQLConnection connection;

    public void handleGetVehicles(Context ctx) {
        connection.getVehicleTable().getVehicles(ctx);
    }

    public void handleGetStats(Context ctx) throws SQLException {
        String dateType = ctx.queryParam("dateType");
        String startDate = ctx.queryParam("startDate");
        String endDate = ctx.queryParam("endDate");

        Map<String, Object> stats = connection.getVehicleTable().getStats(dateType, startDate, endDate);
        ctx.json(new ApiResponse<>(stats));
    }

    public void handleGetColors(Context ctx) throws SQLException {
        String dateType = ctx.queryParam("dateType");
        String startDate = ctx.queryParam("startDate");
        String endDate = ctx.queryParam("endDate");

        Map<String, Object> stats = connection.getVehicleTable().getColors(dateType, startDate, endDate);
        ctx.json(new ApiResponse<>(stats));
    }

    public void handleGetMakes(Context ctx) throws SQLException {
        String dateType = ctx.queryParam("dateType");
        String startDate = ctx.queryParam("startDate");
        String endDate = ctx.queryParam("endDate");

        Map<String, Object> stats = connection.getVehicleTable().getMakes(dateType, startDate, endDate);
        ctx.json(new ApiResponse<>(stats));
    }

    public void handleGetModels(Context ctx) throws SQLException {
        Map<String, Object> stats = connection.getVehicleTable().getModels(ctx);
        ctx.json(stats);
    }

    public void handleGetPlate(Context ctx) {
        try {
            String plate = ctx.pathParam("plate");
            JsonObject vehicleData = VehiclePlateHelper.parseVehicleInfo(plate, connection);

            if (vehicleData == null) {
                ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, "Invalid plate"));
                return;
            }
            ctx.json(new ApiResponse<>(vehicleData));
        } catch (Exception ex) {
            log.error("An error occurred while fetching plate information", ex);
            ctx.status(500).json(new ErrorResponse(ErrorType.SERVER_ERROR, "An error has occurred"));
        }
    }
}
