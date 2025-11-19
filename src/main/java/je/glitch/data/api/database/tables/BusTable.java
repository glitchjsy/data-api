package je.glitch.data.api.database.tables;

import com.zaxxer.hikari.HikariDataSource;
import je.glitch.data.api.modelsnew.entities.BusStopEntity;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class BusTable implements ITable {
    private final HikariDataSource dataSource;

    public List<BusStopEntity> getBusStops() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM busStops");

            try (ResultSet result = stmt.executeQuery()) {
                List<BusStopEntity> stops = new ArrayList<>();

                while (result.next()) {
                    stops.add(BusStopEntity.fromResultSet(result));
                }
                return stops;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    public BusStopEntity getBusStopByStopNumber(String code) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM busStops WHERE stopNumber = ?");
            stmt.setString(1, code);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return BusStopEntity.fromResultSet(result);
                }
            }
            return null;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}
