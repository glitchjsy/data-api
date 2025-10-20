package je.glitch.data.api.database.tables;

import com.zaxxer.hikari.HikariDataSource;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.*;

@RequiredArgsConstructor
public class CourtTable {
    private final HikariDataSource dataSource;

    public void getMagistratesHearings(Context ctx) {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(50);

        List<Object> params = new ArrayList<>();
        StringBuilder query = new StringBuilder("FROM magistratesCourtHearings");

        addFilter(query, params, "courtRoom LIKE BINARY ?", ctx.queryParam("courtRoom"));
        addFilter(query, params, "hearingPurpose LIKE BINARY ?", ctx.queryParam("hearingPurpose"));
        addFilter(query, params, "defendant LIKE BINARY ?", ctx.queryParam("defendant"));
        addFilter(query, params, "appearanceDate >= ?", ctx.queryParam("startDate"));
        addFilter(query, params, "appearanceDate <= ?", ctx.queryParam("endDate"));

        int offset = (page - 1) * limit;

        try (Connection conn = dataSource.getConnection()) {
            int totalItems = fetchSingleInt(conn, "SELECT COUNT(*) " + query, params);
            int totalPages = (int) Math.ceil((double) totalItems / limit);

            List<Map<String, Object>> results = fetchRows(conn,
                    "SELECT * " + query + " ORDER BY appearanceDate DESC LIMIT ? OFFSET ?",
                    params, limit, offset);

            ctx.json(Map.of(
                    "pagination", Map.of(
                            "page", page,
                            "limit", limit,
                            "totalPages", totalPages,
                            "totalItems", totalItems
                    ),
                    "results", results
            ));
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching hearings: " + ex.getMessage(), ex);
        }
    }

    public void getMagistratesResults(Context ctx) {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(50);

        List<Object> params = new ArrayList<>();
        StringBuilder query = new StringBuilder("FROM magistratesCourtResults");

        addFilter(query, params, "courtRoom LIKE BINARY ?", ctx.queryParam("courtRoom"));
        addFilter(query, params, "hearingPurpose LIKE BINARY ?", ctx.queryParam("hearingPurpose"));
        addFilter(query, params, "defendant LIKE BINARY ?", ctx.queryParam("defendant"));
        addFilter(query, params, "appearanceDate >= ?", ctx.queryParam("startDate"));
        addFilter(query, params, "appearanceDate <= ?", ctx.queryParam("endDate"));

        int offset = (page - 1) * limit;

        String offencesQuery = "SELECT offence FROM magistratesCourtResultOffences WHERE resultId=?";

        try (Connection conn = dataSource.getConnection()) {
            int totalItems = fetchSingleInt(conn, "SELECT COUNT(*) " + query, params);
            int totalPages = (int) Math.ceil((double) totalItems / limit);

            List<Map<String, Object>> results = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * " + query + " ORDER BY appearanceDate DESC LIMIT ? OFFSET ?")) {

                setParams(stmt, params);
                stmt.setInt(params.size() + 1, limit);
                stmt.setInt(params.size() + 2, offset);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        int resultId = rs.getInt("id");

                        row.put("id", resultId);
                        row.put("appearanceDate", rs.getTimestamp("appearanceDate"));
                        row.put("video", rs.getString("video"));
                        row.put("hearingPurpose", rs.getString("hearingPurpose"));
                        row.put("result", rs.getString("result"));
                        row.put("remandedOrBailed", rs.getString("remandedOrBailed"));
                        row.put("nextAppearanceDate", rs.getTimestamp("nextAppearanceDate"));
                        row.put("courtRoom", rs.getString("courtRoom"));
                        row.put("lawOfficer", rs.getString("lawOfficer"));
                        row.put("defendant", rs.getString("defendant"));
                        row.put("magistrate", rs.getString("magistrate"));

                        // Fetch offences
                        List<String> offences = new ArrayList<>();
                        try (PreparedStatement offencesStmt = conn.prepareStatement(offencesQuery)) {
                            offencesStmt.setInt(1, resultId);
                            try (ResultSet ors = offencesStmt.executeQuery()) {
                                while (ors.next()) {
                                    offences.add(ors.getString("offence"));
                                }
                            }
                        }
                        row.put("offences", offences);
                        results.add(row);
                    }
                }
            }

            ctx.json(Map.of(
                    "pagination", Map.of(
                            "page", page,
                            "limit", limit,
                            "totalPages", totalPages,
                            "totalItems", totalItems
                    ),
                    "results", results
            ));

        } catch (Exception ex) {
            throw new RuntimeException("Error fetching results: " + ex.getMessage(), ex);
        }
    }

    public Object fetchDistinctColumns(String table, String[] columns) {
        try (Connection conn = dataSource.getConnection()) {
            // Map of column name -> list of distinct values
            var result = new java.util.HashMap<String, List<Object>>();
            for (String col : columns) {
                List<Object> values = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT " + col + " FROM " + table);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        values.add(rs.getObject(1));
                    }
                }
                result.put(col, values);
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching distinct values from " + table + ": " + ex.getMessage(), ex);
        }
    }

    // Helper methods
    private static void addFilter(StringBuilder query, List<Object> params, String condition, String value) {
        if (value != null && !value.isEmpty()) {
            query.append(query.toString().contains("WHERE") ? " AND " : " WHERE ").append(condition);
            params.add(condition.contains("LIKE") ? "%" + value + "%" : value);
        }
    }

    private static int fetchSingleInt(Connection conn, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static List<Map<String, Object>> fetchRows(Connection conn, String sql, List<Object> params, int limit, int offset) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, params);
            stmt.setInt(params.size() + 1, limit);
            stmt.setInt(params.size() + 2, offset);

            List<Map<String, Object>> rows = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
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
