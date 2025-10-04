package je.glitch.data.api.models;

import je.glitch.data.api.models.enums.VehicleFuelType;
import je.glitch.data.api.models.enums.VehicleType;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Data
public class Vehicle {
    private final int id;
    private final Timestamp firstRegisteredAt;
    private final Timestamp firstRegisteredInJerseyAt;
    private final String make;
    private final String model;
    private final VehicleType type;
    private final String color;
    private final int cylinderCapacity;
    private final String weight;
    private final int co2Emissions;
    private final VehicleFuelType fuelType;

    public static Vehicle of(ResultSet result) throws SQLException {
        return new Vehicle(
                result.getInt("id"),
                result.getTimestamp("firstRegisteredAt"),
                result.getTimestamp("firstRegisteredInJerseyAt"),
                result.getString("make"),
                result.getString("model"),
                VehicleType.valueOf(result.getString("type")),
                result.getString("color"),
                result.getInt("cylinderCapacity"),
                result.getString("weight"),
                result.getInt("co2Emissions"),
                VehicleFuelType.valueOf(result.getString("fuelType"))
        );
    }
}
