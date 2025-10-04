package je.glitch.data.api.models.carpark;

import lombok.Data;

@Data
public class CarparkFullDay {
    private final String name;
    private final String code;
    private final String dayOfWeek;
    private final int fullCount;
}