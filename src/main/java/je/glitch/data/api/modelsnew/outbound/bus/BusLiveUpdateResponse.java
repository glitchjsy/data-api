package je.glitch.data.api.modelsnew.outbound.bus;

import lombok.Data;

@Data
public class BusLiveUpdateResponse {
    private final String deviceId;
    private final String assetType;
    private final String registration;
    private final String serviceNumber;
    private final String serviceName;
    private final String serviceOperator;
    private final String originalStartTime;
    private final String timeOfUpdate;
    private final String direction;
    private final double latitude;
    private final double longitude;
    private final int bearing;
    private final int secondsAgo;
}

