package je.glitch.data.api.services;

import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.modelsnew.entities.FoiRequestEntity;
import je.glitch.data.api.modelsnew.outbound.foi.FoiRequestResponse;
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

    public FoiRequestResponse getById(String id) {
        try {
            FoiRequestEntity entity = connection.getFoiTable().getById(Integer.parseInt(id));
            return new FoiRequestResponse(
                    entity.getId(), entity.getPublishDate(), entity.getTitle(), entity.getProducer(),
                    entity.getAuthor(), entity.getRequestText(), entity.getResponseText()
            );
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
