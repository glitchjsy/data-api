package je.glitch.data.api.database.tables;

import com.zaxxer.hikari.HikariDataSource;
import io.javalin.http.Context;
import je.glitch.data.api.utils.Utils;
import je.glitch.data.api.models.Vehicle;
import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.*;

@RequiredArgsConstructor
public class VehicleTable implements ITable {
    private final HikariDataSource dataSource;

    public List<Vehicle> getVehicles(Context ctx) {
        try (Connection connection = dataSource.getConnection()) {
            String dateField = "regInJersey".equals(ctx.queryParam("dateType"))
                    ? "firstRegisteredInJerseyAt"
                    : "firstRegisteredAt";

            Utils.QueryDateResult dateResult = Utils.queryDateSql(
                    dateField,
                    ctx.queryParam("startDate"),
                    ctx.queryParam("endDate")
            );

            StringBuilder query = new StringBuilder("FROM vehicles");
            List<Object> params = new ArrayList<>(dateResult.getDateParams());

            if (!dateResult.getDateSql().isEmpty()) {
                query.append(" ").append(dateResult.getDateSql());
            }

            addFilter(query, params, "make LIKE ?", ctx.queryParam("make"));
            addFilter(query, params, "model LIKE ?", ctx.queryParam("model"));
            addFilter(query, params, "fuelType = ?", ctx.queryParam("fuelType"));
            addFilter(query, params, "color LIKE ?", ctx.queryParam("color"));

            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
            int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(30);
            int offset = (page - 1) * limit;

            int totalItems = fetchSingleInt(connection, "SELECT COUNT(*) " + query, params);
            int totalPages = (int) Math.ceil((double) totalItems / limit);

            List<Map<String, Object>> vehicles = fetchRows(connection,
                    "SELECT * " + query + " LIMIT ? OFFSET ?", params, limit, offset);

            ctx.json(Map.of(
                    "pagination", Map.of("page", page, "limit", limit, "totalPages", totalPages, "totalItems", totalItems),
                    "results", vehicles
            ));
            return new ArrayList<>();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getStats(String dateType, String startDate, String endDate) throws SQLException {
        String dateField = "regInJersey".equals(dateType)
                ? "firstRegisteredInJerseyAt"
                : "firstRegisteredAt";

        Utils.QueryDateResult dateResult = Utils.queryDateSql(dateField, startDate, endDate);

        String sql = """
            SELECT
                COUNT(*) AS totalVehicles,
                COUNT(DISTINCT model) AS distinctModels,
                COUNT(DISTINCT make) AS distinctMakes,
                COUNT(DISTINCT color) AS distinctColors
            FROM vehicles
            """ + dateResult.getDateSql();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            // Set the query parameters dynamically
            List<Object> params = dateResult.getDateParams();
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> results = new HashMap<>();
                    results.put("totalVehicles", rs.getInt("totalVehicles"));
                    results.put("distinctModels", rs.getInt("distinctModels"));
                    results.put("distinctMakes", rs.getInt("distinctMakes"));
                    results.put("distinctColors", rs.getInt("distinctColors"));
                    return results;
                }
            }
        }
        return new HashMap<>();
    }

    public Map<String, Object> getColors(String dateType, String startDate, String endDate) throws SQLException {
        String dateField = "regInJersey".equals(dateType)
                ? "firstRegisteredInJerseyAt"
                : "firstRegisteredAt";

        Utils.QueryDateResult dateResult = Utils.queryDateSql(dateField, startDate, endDate);

        String sql = "SELECT color, COUNT(*) AS occurrences FROM vehicles " + dateResult.getDateSql() + " GROUP BY color ORDER BY occurrences DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            List<Object> params = dateResult.getDateParams();
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                Map<String, Object> results = new HashMap<>();

                while (rs.next()) {
                    String color = rs.getString("color");
                    if (color == null || color.isEmpty()) {
                        color = "Not Specified";
                    }
                    results.put(color, rs.getInt("occurrences"));
                }
                return results;
            }
        }
    }

    public Map<String, Object> getMakes(String dateType, String startDate, String endDate) throws SQLException {
        String dateField = "regInJersey".equals(dateType)
                ? "firstRegisteredInJerseyAt"
                : "firstRegisteredAt";

        Utils.QueryDateResult dateResult = Utils.queryDateSql(dateField, startDate, endDate);

        String sql = "SELECT make, COUNT(*) AS occurrences FROM vehicles " + dateResult.getDateSql() + " GROUP BY make ORDER BY occurrences DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            List<Object> params = dateResult.getDateParams();
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                Map<String, Object> results = new HashMap<>();

                while (rs.next()) {
                    String make = rs.getString("make");
                    results.put(make, rs.getInt("occurrences"));
                }
                return results;
            }
        }
    }

    public Map<String, Object> getModels(Context ctx) {
        try (Connection connection = dataSource.getConnection()) {
            String dateField = "regInJersey".equals(ctx.queryParam("dateType"))
                    ? "firstRegisteredInJerseyAt"
                    : "firstRegisteredAt";

            Utils.QueryDateResult dateResult = Utils.queryDateSql(
                    dateField,
                    ctx.queryParam("startDate"),
                    ctx.queryParam("endDate")
            );

            // Build the SQL query for counting the total items (subquery)
            StringBuilder countQuery = new StringBuilder("SELECT COUNT(*) AS count FROM (");
            countQuery.append("SELECT model, make, COUNT(*) AS occurrences FROM vehicles ");
            countQuery.append(dateResult.getDateSql());
            countQuery.append(" GROUP BY model, make) AS subquery");

            List<Object> params = new ArrayList<>(dateResult.getDateParams());

            int totalItems = fetchSingleInt(connection, countQuery.toString(), params);
            int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(300);
            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
            int offset = (page - 1) * limit;
            int totalPages = (int) Math.ceil((double) totalItems / limit);

            StringBuilder query = new StringBuilder("SELECT model, make, COUNT(*) AS occurrences FROM vehicles ");
            query.append(dateResult.getDateSql());
            query.append(" GROUP BY model, make ORDER BY occurrences DESC LIMIT ? OFFSET ?");

            List<Map<String, Object>> results = fetchRows(connection, query.toString(), params, limit, offset);

            Map<String, Object> occurrences = new HashMap<>();
            for (Map<String, Object> item : results) {
                String model = (String) item.get("model");
                String make = (String) item.get("make");
                long count = (long) item.get("occurrences");
                String key = (model == null || model.isEmpty()) ? "Not Specified" : make + " " + model;
                occurrences.put(key, count);
            }

            return Map.of(
                    "pagination", Map.of(
                            "page", page,
                            "limit", limit,
                            "totalPages", totalPages,
                            "totalItems", totalItems
                    ),
                    "results", occurrences
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of("error", "An error occurred while processing the request.");
        }
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
                    if (value != null && (columnName.equals("firstRegisteredAt") || columnName.equals("firstRegisteredInJerseyAt"))) {
                        value = value.toString().substring(0, 10); // Just the date part
                    }

                    row.put(meta.getColumnName(i), value);
                }
                row.remove("id"); // Exclude the "id" field if needed
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
