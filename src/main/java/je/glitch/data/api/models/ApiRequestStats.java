package je.glitch.data.api.models;

import lombok.Data;

@Data
public class ApiRequestStats {
    private final long totalAllTime;
    private final long total24Hours;
    private final long total7Days;
    private final long total30Days;
}
