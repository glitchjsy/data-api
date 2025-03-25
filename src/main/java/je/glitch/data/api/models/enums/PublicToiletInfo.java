package je.glitch.data.api.models.enums;

import lombok.Data;

import java.util.List;

@Data
public class PublicToiletInfo {
    private final Integer cubicles;
    private final Integer handDryers;
    private final Integer urinals;
    private final Integer sinks;
    private final List<PeriodProduct> periodProducts;
}