package je.glitch.data.api.services;

import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.FoiRequest;
import je.glitch.data.api.utils.ErrorType;
import je.glitch.data.api.utils.HttpException;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class FoiService {
    private final MySQLConnection connection;

    public void getRequests(Context ctx) {
        connection.getFoiTable().getRequests(ctx);
    }

    public FoiRequest getById(String id) {
        try {
            return connection.getFoiTable().getById(Integer.parseInt(id));
        } catch (NumberFormatException ex) {
            throw new HttpException(ErrorType.INVALID_REQUEST, 400, "The id provided is invalid");
        }
    }

    public Map<String, Object> getStats(String startDate, String endDate) throws SQLException {
        return connection.getFoiTable().getStats(startDate, endDate);
    }

    public List<String> getAuthors() {
        return connection.getFoiTable().getAllDistinctAuthors();
    }

    public List<String> getProducers() {
        return connection.getFoiTable().getAllDistinctProducers();
    }
}
