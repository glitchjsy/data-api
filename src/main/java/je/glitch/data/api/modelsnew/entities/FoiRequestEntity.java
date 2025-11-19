package je.glitch.data.api.modelsnew.entities;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
public class FoiRequestEntity {
    private int id;
    private String publishDate;
    private String title;
    private String producer;
    private String author;
    private String requestText;
    private String responseText;

    public static FoiRequestEntity fromResultSet(ResultSet rs) throws SQLException {
        FoiRequestEntity request = new FoiRequestEntity();
        request.setId(rs.getInt("id"));
        request.setPublishDate(rs.getString("publishDate"));
        request.setTitle(rs.getString("title"));
        request.setProducer(rs.getString("producer"));
        request.setAuthor(rs.getString("author"));
        request.setRequestText(rs.getString("requestText"));
        request.setResponseText(rs.getString("responseText"));
        return request;
    }
}
