package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.models.ApiResponse;
import je.glitch.data.api.modelsnew.outbound.bus.BusStopLiveDepartureResponse;
import je.glitch.data.api.modelsnew.outbound.bus.BusStopResponse;
import je.glitch.data.api.modelsnew.outbound.bus.BusStopWithDeparturesResponse;
import je.glitch.data.api.services.BusService;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.utils.ErrorType;
import je.glitch.data.api.utils.Utils;
import je.glitch.data.api.utils.ratelimit.RateLimitConfig;
import je.glitch.data.api.utils.ratelimit.RateLimitType;
import je.glitch.data.api.utils.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
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

    public void handleGetRoutes(Context ctx) {
        ctx.json(new ApiResponse<>(service.getBusRoutes()));
    }

    public void handleGetStop(Context ctx) {
        String idParam = ctx.pathParam("id");
        Integer id = Utils.parseInt(idParam);

        if (id == null) {
            ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, "Invalid stop ID"));
            return;
        }

        BusStopResponse stop = service.getBusStop(id);
        if (stop == null) {
            ctx.status(500).json(new ErrorResponse(ErrorType.NOT_FOUND, "Bus stop not found"));
            return;
        }

        List<BusStopLiveDepartureResponse> departures = service.getStopLiveDepartures(id);

        if (departures == null) {
            departures = new ArrayList<>();
        }
        ctx.json(new ApiResponse<>(new BusStopWithDeparturesResponse(stop, departures)));
    }

    public void handleGetLiveUpdates(Context ctx) {
        Integer secondsAgo = Utils.optionalInt(ctx.queryParam("secondsAgo"));
        Double lat = Utils.optionalDouble(ctx.queryParam("lat"));
        Double lon = Utils.optionalDouble(ctx.queryParam("lon"));
        Integer radius = Utils.optionalInt(ctx.queryParam("radius"));
        Integer limitTo = Utils.optionalInt(ctx.queryParam("limitTo"));

        if (secondsAgo == null) {
            secondsAgo = 5000;
        }
        if ((lat != null && lon == null) || (lon != null && lat == null)) {
            ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, "Latitude and longitude must both be provided"));
            return;
        }
        if ((radius != null || limitTo != null) && (lat == null || lon == null)) {
            ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, "lat and lon are required when using radius or limitTo"));
            return;
        }
        if (lat != null && lon != null) {
            if (radius == null) {
                radius = 500;
            }
            if (limitTo == null) {
                limitTo = 10;
            }
        }

        var updates = service.getLiveUpdates(secondsAgo, lat, lon, radius, limitTo);
        ctx.json(new ApiResponse<>(updates));
    }

    public void handleGetMinUpdates(Context ctx) {
        Integer secondsAgo = Utils.optionalInt(ctx.queryParam("secondsAgo"));
        Double lat = Utils.optionalDouble(ctx.queryParam("lat"));
        Double lon = Utils.optionalDouble(ctx.queryParam("lon"));
        Integer radius = Utils.optionalInt(ctx.queryParam("radius"));
        Integer limitTo = Utils.optionalInt(ctx.queryParam("limitTo"));

        if (secondsAgo == null) {
            secondsAgo = 5000;
        }
        if ((lat != null && lon == null) || (lon != null && lat == null)) {
            ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, "Latitude and longitude must both be provided"));
            return;
        }
        if ((radius != null || limitTo != null) && (lat == null || lon == null)) {
            ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, "lat and lon are required when using radius or limitTo"));
            return;
        }
        if (lat != null && lon != null) {
            if (radius == null) {
                radius = 500;
            }
            if (limitTo == null) {
                limitTo = 10;
            }
        }

        var updates = service.getMinUpdates(secondsAgo, lat, lon, radius, limitTo);
        ctx.json(new ApiResponse<>(updates));
    }
}
