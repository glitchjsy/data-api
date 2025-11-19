package je.glitch.data.api.modelsnew.inbound;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import java.util.List;

@Data
public class BusStopLiveInbound {

    @Data
    public static class LiveDeparture {
        @SerializedName("StopNumber")
        private int stopNumber;

        @SerializedName("ServiceNumber")
        private String serviceNumber;

        @SerializedName("Destination")
        private String destination;

        @SerializedName("ETA")
        private String eta;
    }

    private List<LiveDeparture> departures;
}
