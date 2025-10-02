package je.glitch.data.api.models;

import je.glitch.data.api.models.enums.PublicToiletFacilities;
import je.glitch.data.api.models.enums.PublicToiletInfo;
import je.glitch.data.api.models.enums.PublicToiletTenure;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PublicToilet {
    private final String id;
    private final Date createdAt;
    private final Date updatedAt;
    private final String name;
    private final String parish;
    private final double latitude;
    private final double longitude;
    private final Integer buildDate;
    private final List<PublicToiletFacilities> facilities;
    private final Company owner;
    private final PublicToiletInfo female;
    private final PublicToiletInfo male;
}