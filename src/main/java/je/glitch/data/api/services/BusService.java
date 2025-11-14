package je.glitch.data.api.services;

import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.BusStop;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BusService {
    private final MySQLConnection connection;

    public List<BusStop> getBusStops() {
        return connection.getBusTable().getBusStops();
    }
}
