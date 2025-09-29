package je.glitch.data.api.models;

import je.glitch.data.api.models.enums.RecyclingService;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
public class RecyclingCentre {
    private final String id;
    private final Timestamp createdAt;
    private final String location;
    private final String parish;
    private final double latitude;
    private final double longitude;
    private final String notes;
    private final List<RecyclingService> services;
}