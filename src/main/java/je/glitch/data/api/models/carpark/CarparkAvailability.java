package je.glitch.data.api.models.carpark;

import lombok.Data;

@Data
public class CarparkAvailability {
    private final String name;
    private final String code;
    private final int year;
    private final int month;
    private final double availabilityPercentage;
}