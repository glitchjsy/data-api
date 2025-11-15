package je.glitch.data.api.utils.ratelimit;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public class RateLimitConfig {
    private final int maxRequests;
    private final long resetPeriodMillis;

    public RateLimitConfig(int maxRequests, long resetPeriod, TimeUnit unit) {
        this.maxRequests = maxRequests;
        this.resetPeriodMillis = unit.toMillis(resetPeriod);
    }
}