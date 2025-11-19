package je.glitch.data.api.modelsnew.outbound.bus;

import lombok.Data;

@Data
public class BusStopLiveDepartureResponse {
    private final String destination;
    private final String eta;
    private final String serviceNumber;
    private final int stopNumber;
}
