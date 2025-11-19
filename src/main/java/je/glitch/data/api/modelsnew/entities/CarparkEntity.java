package je.glitch.data.api.modelsnew.entities;

import je.glitch.data.api.models.enums.CarparkPaymentMethod;
import je.glitch.data.api.models.enums.CarparkSurfaceType;
import je.glitch.data.api.models.enums.CarparkType;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CarparkEntity {
    private String id;
    private Timestamp createdAt;
    private String name;
    private String payByPhoneCode;
    private CompanyEntity owner;
    private CarparkType type;
    private CarparkSurfaceType surfaceType;
    private boolean multiStorey;
    private double latitude;
    private double longitude;
    private List<CarparkPaymentMethod> paymentMethods;
    private int spaces;
    private int disabledSpaces;
    private int parentChildSpaces;
    private int electricChargingSpaces;
    private String liveTrackingCode;
    private String notes;

    public static CarparkEntity fromResultSet(ResultSet rs) throws SQLException {
        CarparkEntity carpark = new CarparkEntity();
        carpark.setId(rs.getString("id"));
        carpark.setCreatedAt(rs.getTimestamp("createdAt"));
        carpark.setName(rs.getString("name"));
        carpark.setPayByPhoneCode(rs.getString("payByPhoneCode"));
        carpark.setOwner(new CompanyEntity(rs.getString("ownerId"), rs.getString("ownerName")));
        carpark.setType(CarparkType.valueOf(rs.getString("type")));
        carpark.setSurfaceType(CarparkSurfaceType.valueOf(rs.getString("surfaceType")));
        carpark.setMultiStorey(rs.getBoolean("multiStorey"));
        carpark.setLatitude(rs.getDouble("latitude"));
        carpark.setLongitude(rs.getDouble("longitude"));
        carpark.setPaymentMethods(parsePaymentMethods(rs.getString("paymentMethods")));
        carpark.setSpaces(rs.getInt("spaces"));
        carpark.setDisabledSpaces(rs.getInt("disabledSpaces"));
        carpark.setParentChildSpaces(rs.getInt("parentChildSpaces"));
        carpark.setElectricChargingSpaces(rs.getInt("electricChargingSpaces"));
        carpark.setLiveTrackingCode(rs.getString("liveTrackingCode"));
        carpark.setNotes(rs.getString("notes"));
        return carpark;
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
