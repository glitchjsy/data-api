package je.glitch.data.api.database.tables;

import com.zaxxer.hikari.HikariDataSource;
import je.glitch.data.api.models.BusStop;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class BusTable implements ITable {
    private final HikariDataSource dataSource;

    public List<BusStop> getBusStops() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM busStops");

            try (ResultSet result = stmt.executeQuery()) {
                List<BusStop> stops = new ArrayList<>();

                while (result.next()) {
                    stops.add(BusStop.of(result));
                }
                return stops;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }
}
