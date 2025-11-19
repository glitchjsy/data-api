package je.glitch.data.api.modelsnew.entities;

import lombok.Data;

@Data
public class EndpointRequestStatEntity {
    private final String path;
    private final long total;
}
