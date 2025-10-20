package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.ApiResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CourtController {
    private final MySQLConnection connection;

    public void handleGetMagistratesHearings(Context ctx) {
        connection.getCourtTable().getMagistratesHearings(ctx);
    }

    public void handleGetMagistratesResults(Context ctx) {
        connection.getCourtTable().getMagistratesResults(ctx);
    }

    public void handleGetDistinctMagistratesHearingFields(Context ctx) {
        String[] columns = {"courtRoom", "hearingPurpose"};
        ctx.json(new ApiResponse<>(
                connection.getCourtTable().fetchDistinctColumns("magistratesCourtHearings", columns)
        ));
    }

    public void handleGetDistinctMagistratesResultFields(Context ctx) {
        String[] columns = {"courtRoom", "hearingPurpose", "video", "magistrate", "remandedOrBailed"};

        @SuppressWarnings("unchecked")
        Map<String, List<Object>> result = (Map<String, List<Object>>) connection
                .getCourtTable()
                .fetchDistinctColumns("magistratesCourtResults", columns);

        @SuppressWarnings("unchecked")
        Map<String, List<Object>> offenceResult = (Map<String, List<Object>>) connection
                .getCourtTable()
                .fetchDistinctColumns("magistratesCourtResultOffences", new String[]{"offence"});

        result.put("offences", offenceResult.get("offence"));
        ctx.json(new ApiResponse<>(result));
    }
}
