package je.glitch.data.api.models;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Session implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Date loginTime;
    private final String userId;
}
