package je.glitch.data.api.modelsnew.outbound.bus;

import lombok.Data;

@Data
public class BusStopResponse {
    private final String id;
    private final String name;
    private final String stopNumber;
    private final double latitude;
    private final double longitude;
    private final boolean shelter;
}
