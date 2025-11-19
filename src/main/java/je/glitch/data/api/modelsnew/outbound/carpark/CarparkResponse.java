package je.glitch.data.api.modelsnew.outbound.carpark;

import je.glitch.data.api.models.Company;
import je.glitch.data.api.models.enums.CarparkPaymentMethod;
import je.glitch.data.api.models.enums.CarparkSurfaceType;
import je.glitch.data.api.models.enums.CarparkType;
import je.glitch.data.api.modelsnew.entities.CompanyEntity;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class CarparkResponse {
    private final String id;
    private final Timestamp createdAt;
    private final String name;
    private final String payByPhoneCode;
    private final CompanyEntity owner;
    private final CarparkType type;
    private final CarparkSurfaceType surfaceType;
    private final boolean multiStorey;
    private final double latitude;
    private final double longitude;
    private final List<CarparkPaymentMethod> paymentMethods;
    private final int spaces;
    private final int disabledSpaces;
    private final int parentChildSpaces;
    private final int electricChargingSpaces;
    private final String liveTrackingCode;
    private final String notes;
}
