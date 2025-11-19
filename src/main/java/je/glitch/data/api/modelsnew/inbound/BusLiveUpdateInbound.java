package je.glitch.data.api.modelsnew.inbound;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

public class BusLiveUpdateInbound {

    @Data
    public static class Update {
        @SerializedName("DeviceId")
        private String deviceId;

        @SerializedName("AssetType")
        private String assetType;

        @SerializedName("AssetRegistrationNumber")
        private String registration;

        @SerializedName("ServiceNumber")
        private String serviceNumber;

        @SerializedName("ServiceName")
        private String serviceName;

        @SerializedName("ServiceOperator")
        private String serviceOperator;

        @SerializedName("OriginalStartTime")
        private String originalStartTime;

        @SerializedName("TimeOfUpdate")
        private String timeOfUpdate;

        @SerializedName("Direction")
        private String direction;

        @SerializedName("Latitude")
        private double latitude;

        @SerializedName("Longitude")
        private double longitude;

        @SerializedName("Bearing")
        private int bearing;

        @SerializedName("SecondsAgo")
        private int secondsAgo;
    }

    @Data
    public static class MinUpdate {
        @SerializedName("bus")
        private String bus;

        @SerializedName("line")
        private String line;

        @SerializedName("cat")
        private String category;

        @SerializedName("lat")
        private double latitude;

        @SerializedName("lon")
        private double longitude;

        @SerializedName("bearing")
        private int bearing;

        @SerializedName("direction")
        private String direction;

        @SerializedName("time")
        private String time;

        @SerializedName("age")
        private int age;
    }
}
