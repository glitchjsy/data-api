package je.glitch.data.api.modelsnew.outbound.bus;

import lombok.Data;

@Data
public class BusLiveUpdateMinResponse {
    private final String bus;
    private final String line;
    private final String category;
    private final double latitude;
    private final double longitude;
    private final int bearing;
    private final String direction;
    private final String time;
    private final int age;
}

