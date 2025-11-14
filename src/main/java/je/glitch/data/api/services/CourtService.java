package je.glitch.data.api.services;

import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CourtService {

    private final MySQLConnection connection;

    public void getMagistratesHearings(Context ctx) {
        connection.getCourtTable().getMagistratesHearings(ctx);
    }

    public void getMagistratesResults(Context ctx) {
        connection.getCourtTable().getMagistratesResults(ctx);
    }

    public Object getDistinctMagistratesHearingFields() {
        String[] columns = {"courtRoom", "hearingPurpose"};
        return connection.getCourtTable().fetchDistinctColumns("magistratesCourtHearings", columns);
    }

    public Map<String, List<Object>> getDistinctMagistratesResultFields() {
        String[] resultColumns = {"courtRoom", "hearingPurpose", "video", "magistrate", "remandedOrBailed"};
        String[] offenceColumns = {"offence"};

        @SuppressWarnings("unchecked")
        Map<String, List<Object>> result = (Map<String, List<Object>>) connection
                .getCourtTable()
                .fetchDistinctColumns("magistratesCourtResults", resultColumns);

        @SuppressWarnings("unchecked")
        Map<String, List<Object>> offences = (Map<String, List<Object>>) connection
                .getCourtTable()
                .fetchDistinctColumns("magistratesCourtResultOffences", offenceColumns);

        result.put("offences", offences.get("offence"));
        return result;
    }
}
