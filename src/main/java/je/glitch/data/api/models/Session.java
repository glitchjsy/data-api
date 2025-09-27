package je.glitch.data.api.models;

import lombok.Data;

import java.util.Date;

@Data
public class Session {
    private final Date loginTime;
    private final String userId;
}
