package je.glitch.data.api.modelsnew.entities;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Data
public class BusStopEntity {
    private String id;
    private Timestamp createdAt;
    private String name;
    private String stopNumber;
    private double latitude;
    private double longitude;
    private boolean shelter;

    public static BusStopEntity fromResultSet(ResultSet rs) throws SQLException {
        BusStopEntity stop = new BusStopEntity();
        stop.setId(rs.getString("id"));
        stop.setCreatedAt(rs.getTimestamp("createdAt"));
        stop.setName(rs.getString("name"));
        stop.setStopNumber(rs.getString("stopNumber"));
        stop.setLatitude(rs.getDouble("latitude"));
        stop.setLongitude(rs.getDouble("longitude"));
        stop.setShelter(rs.getBoolean("shelter"));
        return stop;
    }
}
