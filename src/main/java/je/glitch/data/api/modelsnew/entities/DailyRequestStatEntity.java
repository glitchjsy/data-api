package je.glitch.data.api.modelsnew.entities;

import lombok.Data;

@Data
public class DailyRequestStatEntity {
    private final String day;   // e.g. "2025-09-01"
    private final String authStatus;
    private final long total;
}
