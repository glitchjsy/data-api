package je.glitch.data.api.utils.ratelimit;

import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.HttpStatus;
import je.glitch.data.api.Server;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.utils.ErrorType;
import je.glitch.data.api.utils.HttpException;
import je.glitch.data.api.utils.Utils;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Based off the Javalin rate limiter.
 */
public class RateLimiter {

    private static class Counter {
        private int count;
        private long resetAt;

        public Counter(long resetAt) {
            this.count = 0;
            this.resetAt = resetAt;
        }

        public synchronized int incrementAndGet(long now, long resetPeriod) {
            if (now >= resetAt) {
                count = 1;
                resetAt = now + resetPeriod;
            } else {
                count++;
            }
            return count;
        }

        public synchronized int getCount() {
            return count;
        }

        public synchronized long getResetAt() {
            return resetAt;
        }
    }

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<RateLimitType, RateLimitConfig> configs;
    private final Function<Context, String> keyResolver;

    public RateLimiter(Map<RateLimitType, RateLimitConfig> configs) {
        this(configs, RateLimiter::defaultKeyResolver);
    }

    public RateLimiter(Map<RateLimitType, RateLimitConfig> configs, Function<Context, String> keyResolver) {
        this.configs = configs;
        this.keyResolver = keyResolver;
    }

    public void handleRequest(Context ctx) {
        RateLimitType type = determineClientType(ctx, Server.INSTANCE.getConnection());
        RateLimitConfig config = configs.get(type);

        String key = type + ":" + keyResolver.apply(ctx); // e.g. type + IP + path
        long now = Instant.now().toEpochMilli();

        Counter counter = counters.computeIfAbsent(key, k -> new Counter(now + config.getResetPeriodMillis()));
        int currentCount = counter.incrementAndGet(now, config.getResetPeriodMillis());

        // Set RateLimit headers
        ctx.header("X-RateLimit-Limit", String.valueOf(config.getMaxRequests()));
        ctx.header("X-RateLimit-Remaining", String.valueOf(Math.max(config.getMaxRequests() - currentCount, 0)));
        ctx.header("X-RateLimit-Reset", String.valueOf(counter.getResetAt() / 1000));

        if (currentCount > config.getMaxRequests()) {
            throw new HttpException(ErrorType.RATE_LIMITED, 429, "Rate limit exceeded. Try again in " + ((counter.getResetAt() - now) / 1000) + " seconds.");
        }
    }

    public static String defaultKeyResolver(Context ctx) {
        String ip = ctx.header("X-Forwarded-For") != null
                ? ctx.header("X-Forwarded-For").split(",")[0].trim()
                : ctx.ip();
        return ip + ":" + ctx.method() + ":" + ctx.path();
    }

    public static RateLimitType determineClientType(Context ctx, MySQLConnection connection) {
        if (Utils.isAuthenticatedPublic(ctx, connection)) {
            return RateLimitType.AUTHENTICATED;
        }
        // TODO: Check database for if the caller is a partner
        return RateLimitType.DEFAULT;
    }
}
