package je.glitch.data.api.database.tables;

import com.zaxxer.hikari.HikariDataSource;
import io.javalin.http.Context;
import je.glitch.data.api.models.FoiRequest;
import je.glitch.data.api.models.User;
import je.glitch.data.api.utils.ErrorType;
import je.glitch.data.api.utils.HttpException;
import je.glitch.data.api.utils.Utils;
import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.*;

@RequiredArgsConstructor
public class FoiTable implements ITable {
    private final HikariDataSource dataSource;

    public FoiRequest getById(int id) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM foiRequests WHERE id = ?");
            stmt.setInt(1, id);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return FoiRequest.of(result);
                }
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public List<FoiRequest> getRequests(Context ctx) {
        try (Connection connection = dataSource.getConnection()) {
            Utils.QueryDateResult dateResult = Utils.queryDateSql(
                    "publishDate",
                    ctx.queryParam("startDate"),
                    ctx.queryParam("endDate")
            );

            boolean includeText = ctx.queryParam("includeText") != null;

            StringBuilder query = new StringBuilder("FROM foiRequests");
            List<Object> params = new ArrayList<>(dateResult.getDateParams());

            if (!dateResult.getDateSql().isEmpty()) {
                query.append(" ").append(dateResult.getDateSql());
            }

            addFilter(query, params, "title LIKE ?", ctx.queryParam("title"));
            addFilter(query, params, "producer LIKE ?", ctx.queryParam("producer"));
            addFilter(query, params, "author = ?", ctx.queryParam("author"));
            addFilter(query, params, "requestText LIKE ?", ctx.queryParam("requestText"));
            addFilter(query, params, "responseText LIKE ?", ctx.queryParam("responseText"));

            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
            int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(includeText ? 30 : 60);
            int offset = (page - 1) * limit;

            int totalItems = fetchSingleInt(connection, "SELECT COUNT(*) " + query, params);
            int totalPages = (int) Math.ceil((double) totalItems / limit);

            String selectColumns = includeText ? "*" : "id, publishDate, title, producer, author";

            List<Map<String, Object>> requests = fetchRows(connection,
                    "SELECT " + selectColumns + " " + query + " LIMIT ? OFFSET ?", params, limit, offset);

            ctx.json(Map.of(
                    "pagination", Map.of("page", page, "limit", limit, "totalPages", totalPages, "totalItems", totalItems),
                    "results", requests
            ));
            return new ArrayList<>();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new HttpException(ErrorType.SERVER_ERROR, 500, ex.getMessage());
        }
    }

    public List<String> getAllDistinctAuthors() {
        List<String> authors = new ArrayList<>();
        String sql = "SELECT DISTINCT author FROM foiRequests";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {

            while (result.next()) {
                authors.add(result.getString("author"));
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return authors;
    }


    public List<String> getAllDistinctProducers() {
        List<String> authors = new ArrayList<>();
        String sql = "SELECT DISTINCT producer FROM foiRequests";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {

            while (result.next()) {
                authors.add(result.getString("producer"));
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return authors;
    }


    public Map<String, Object> getStats(String startDate, String endDate) throws SQLException {
        Utils.QueryDateResult dateResult = Utils.queryDateSql("publishDate", startDate, endDate);

        // Main summary query
        String sqlSummary = """
    SELECT
        COUNT(*) AS totalRequests,
        COUNT(DISTINCT producer) AS distinctProducers,
        COUNT(DISTINCT author) AS distinctAuthors
    FROM foiRequests
    """ + dateResult.getDateSql();

        // Totals per year query
        String sqlPerYear = """
    SELECT
        YEAR(publishDate) AS year,
        COUNT(*) AS total
    FROM foiRequests
    """ + dateResult.getDateSql() + " GROUP BY YEAR(publishDate) ORDER BY YEAR(publishDate)";

        // Top authors query
        String sqlTopAuthors = """
    SELECT
        author,
        COUNT(*) AS total_requests
    FROM foiRequests
    """ + dateResult.getDateSql() + """
    GROUP BY author
    ORDER BY total_requests DESC
    LIMIT 10
    """;

        Map<String, Object> results = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            // Summary
            try (PreparedStatement stmt = conn.prepareStatement(sqlSummary)) {
                List<Object> params = dateResult.getDateParams();
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        results.put("totalRequests", rs.getInt("totalRequests"));
                        results.put("distinctProducers", rs.getInt("distinctProducers"));
                        results.put("distinctAuthors", rs.getInt("distinctAuthors"));
                    }
                }
            }

            // Totals per year
            try (PreparedStatement stmt = conn.prepareStatement(sqlPerYear)) {
                List<Object> params = dateResult.getDateParams();
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                Map<Integer, Integer> totalsPerYear = new HashMap<>();
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        totalsPerYear.put(rs.getInt("year"), rs.getInt("total"));
                    }
                }
                results.put("totalsPerYear", totalsPerYear);
            }

            // Top authors (Map<String, Integer>)
            try (PreparedStatement stmt = conn.prepareStatement(sqlTopAuthors)) {
                List<Object> params = dateResult.getDateParams();
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                Map<String, Integer> topAuthors = new LinkedHashMap<>(); // preserves order
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        topAuthors.put(rs.getString("author"), rs.getInt("total_requests"));
                    }
                }
                results.put("topAuthors", topAuthors);
            }
        }

        return results;
    }

    private static void addFilter(StringBuilder query, List<Object> params, String condition, String value) {
        if (value != null) {
            query.append(query.toString().contains("WHERE") ? " AND " : " WHERE ").append(condition);

            String newValue = value;
            if (condition.contains("LIKE")) {
                newValue = "%" + value + "%";
            }
            params.add(newValue);
        }
    }

    private static int fetchSingleInt(Connection conn, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, params);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static List<Map<String, Object>> fetchRows(Connection conn, String sql, List<Object> params, int limit, int offset) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, params);
            stmt.setInt(params.size() + 1, limit);
            stmt.setInt(params.size() + 2, offset);
            ResultSet rs = stmt.executeQuery();

            List<Map<String, Object>> rows = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    Object value = rs.getObject(i);

                    // Format date fields to only YYYY-MM-DD
                    String columnName = meta.getColumnName(i);
                    if (value != null && (columnName.equals("publishDate"))) {
                        value = value.toString().substring(0, 10); // Just the date part
                    }

                    row.put(meta.getColumnName(i), value);
                }
                rows.add(row);
            }
            return rows;
        }
    }

    private static void setParams(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }
}
