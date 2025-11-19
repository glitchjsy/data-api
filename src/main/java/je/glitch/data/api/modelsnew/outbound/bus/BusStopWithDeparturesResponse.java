package je.glitch.data.api.modelsnew.outbound.bus;

import lombok.Getter;

import java.util.List;

public class BusStopWithDeparturesResponse extends BusStopResponse {
    @Getter
    private final List<BusStopLiveDepartureResponse> departures;

    public BusStopWithDeparturesResponse(BusStopResponse stop, List<BusStopLiveDepartureResponse> departures) {
        super(stop.getId(), stop.getName(), stop.getStopNumber(), stop.getLatitude(), stop.getLongitude(), stop.isShelter());
        this.departures = departures;
    }
}
