package je.glitch.data.api.models;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
public class FoiRequest {
    private final int id;
    private final String publishDate;
    private final String title;
    private final String producer;
    private final String author;
    private final String requestText;
    private final String responseText;

    public static FoiRequest of(ResultSet result) throws SQLException {
        return new FoiRequest(
                result.getInt("id"),
                result.getString("publishDate"),
                result.getString("title"),
                result.getString("producer"),
                result.getString("author"),
                result.getString("requestText"),
                result.getString("responseText")
        );
    }
}
