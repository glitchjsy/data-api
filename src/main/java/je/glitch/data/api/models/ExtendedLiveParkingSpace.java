package je.glitch.data.api.models;

import lombok.Getter;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@ToString
public class ExtendedLiveParkingSpace extends LiveParkingSpace {
    private final Carpark carparkInfo;

    public ExtendedLiveParkingSpace(String id, String createdAt, String name, String code, int spaces, String status, boolean open, Carpark carparkInfo) {
        super(id, createdAt, name, code, spaces, status, open);
        this.carparkInfo = carparkInfo;
    }

    public static ExtendedLiveParkingSpace of(ResultSet result, Carpark carparkInfo) throws SQLException {
        LiveParkingSpace base = LiveParkingSpace.of(result);
        return new ExtendedLiveParkingSpace(
                base.getId(),
                base.getCreatedAt(),
                base.getName(),
                base.getCode(),
                base.getSpaces(),
                base.getStatus(),
                base.isOpen(),
                carparkInfo
        );
    }
}
