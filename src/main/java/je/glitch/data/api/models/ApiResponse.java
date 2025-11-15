package je.glitch.data.api.models;

import lombok.Data;

// only for success responses
// TODO: return if a response was cached
@Data
public class ApiResponse<T> {
    private final T results;
}
