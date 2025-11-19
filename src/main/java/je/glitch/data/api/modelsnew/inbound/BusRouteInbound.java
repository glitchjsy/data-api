package je.glitch.data.api.modelsnew.inbound;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class BusRouteInbound {
    @SerializedName("Number")
    private final String number;

    @SerializedName("Name")
    private final String name;

    @SerializedName("Colour")
    private final String color;
}
