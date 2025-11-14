package je.glitch.data.api.services;

import com.google.gson.JsonObject;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.utils.VehiclePlateHelper;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Map;

import io.javalin.http.Context;

@RequiredArgsConstructor
public class VehicleService {
    private final MySQLConnection connection;

    public void getVehicles(Context ctx) {
        connection.getVehicleTable().getVehicles(ctx);
    }

    public Map<String, Object> getStats(Context ctx) throws SQLException {
        return connection.getVehicleTable().getStats(ctx);
    }

    public Map<String, Object> getColors(Context ctx) throws SQLException {
        return connection.getVehicleTable().getColors(ctx);
    }

    public Map<String, Object> getMakes(Context ctx) throws SQLException {
        return connection.getVehicleTable().getMakes(ctx);
    }

    public Map<String, Object> getModels(Context ctx) throws SQLException {
        return connection.getVehicleTable().getModels(ctx);
    }

    public JsonObject getPlate(String plate) throws Exception {
        return VehiclePlateHelper.parseVehicleInfo(plate, connection);
    }
}
