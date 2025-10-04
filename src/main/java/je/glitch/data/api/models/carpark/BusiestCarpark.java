package je.glitch.data.api.models.carpark;

import lombok.Data;

@Data
public class BusiestCarpark {
    private final String name;
    private final String code;
    private final int timesFull;
}