package je.glitch.data.api.models;

import lombok.Data;

import java.util.Date;

@Data
public class SessionResponse {
    private final Date loginTime;
    private final User user;
}
