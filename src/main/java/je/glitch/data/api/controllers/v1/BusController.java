package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.BusStop;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BusController {
    private final MySQLConnection connection;

    public void handleGetStops(Context ctx) {
        List<BusStop> stops = connection.getBusTable().getBusStops();
        ctx.json(stops);
    }
}
