package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.models.ApiResponse;
import je.glitch.data.api.services.BusService;
import je.glitch.data.api.utils.ratelimit.RateLimitConfig;
import je.glitch.data.api.utils.ratelimit.RateLimitType;
import je.glitch.data.api.utils.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class BusController {
    private final BusService service;
    private final RateLimiter rateLimiter = new RateLimiter(Map.of(
            RateLimitType.DEFAULT, new RateLimitConfig(60, 1, TimeUnit.HOURS),
            RateLimitType.AUTHENTICATED, new RateLimitConfig(150, 1, TimeUnit.HOURS)
    ));

    public void handleGetStops(Context ctx) {
        rateLimiter.handleRequest(ctx);
        ctx.json(new ApiResponse<>(service.getBusStops()));
    }
}
