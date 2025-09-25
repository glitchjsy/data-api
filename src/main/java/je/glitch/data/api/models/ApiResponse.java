package je.glitch.data.api.models;

import lombok.Data;

// only for success responses
@Data
public class ApiResponse<T> {
    private final T results;
}
