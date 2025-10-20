package je.glitch.data.api.models;

import lombok.Data;

@Data
public class DailyRequestStat {
    private final String day;   // e.g. "2025-09-01"
    private final String authStatus;
    private final long total;
}
