package je.glitch.data.api.modelsnew.entities;

import lombok.Data;

@Data
public class ApiRequestStatsEntity {
    private final long totalAllTime;
    private final long total24Hours;
    private final long total7Days;
    private final long total30Days;
}
