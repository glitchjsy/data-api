package je.glitch.data.api.models;

import lombok.Data;

@Data
public class LoginBody {
    private final String email;
    private final String password;
}
