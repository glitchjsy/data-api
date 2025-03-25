package je.glitch.data.api.models;

import je.glitch.data.api.models.enums.CarparkPaymentMethod;
import je.glitch.data.api.models.enums.CarparkSurfaceType;
import je.glitch.data.api.models.enums.CarparkType;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Carpark {
    private final String id;
    private final String createdAt;
    private final String name;
    private final String payByPhoneCode;
    private final Company owner;
    private final CarparkType type;
    private final CarparkSurfaceType surfaceType;
    private final boolean multiStorey;
    private final double latitude;
    private final double longitude;
    private final List<CarparkPaymentMethod> paymentMethods;
    private final int spaces;
    private final int disabledSpaces;
    private final int parentChildSpaces;
    private final int electricChargingSpaces;
    private final String liveTrackingCode;
    private final String notes;

    public static Carpark of(ResultSet result) throws SQLException {
        return new Carpark(
                result.getString("id"),
                result.getString("createdAt"),
                result.getString("name"),
                result.getString("payByPhoneCode"),
                new Company(result.getString("ownerId"), result.getString("ownerName")),
                CarparkType.valueOf(result.getString("type")),
                CarparkSurfaceType.valueOf(result.getString("surfaceType")),
                result.getBoolean("multiStorey"),
                result.getDouble("latitude"),
                result.getDouble("longitude"),
                parsePaymentMethods(result.getString("paymentMethods")),
                result.getInt("spaces"),
                result.getInt("disabledSpaces"),
                result.getInt("parentChildSpaces"),
                result.getInt("electricChargingSpaces"),
                result.getString("liveTrackingCode"),
                result.getString("notes")
        );
    }

    private static List<CarparkPaymentMethod> parsePaymentMethods(String paymentMethods) {
        return paymentMethods == null || paymentMethods.isEmpty()
                ? new ArrayList<>()
                : Arrays.stream(paymentMethods.split(","))
                .map(String::trim)
                .map(CarparkPaymentMethod::valueOf)
                .collect(Collectors.toList());
    }
}
