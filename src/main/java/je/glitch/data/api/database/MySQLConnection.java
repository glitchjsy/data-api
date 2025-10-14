package je.glitch.data.api.database;

import com.zaxxer.hikari.HikariDataSource;
import je.glitch.data.api.database.tables.*;
import lombok.Getter;

@Getter
public class MySQLConnection {
    private final HikariDataSource dataSource = new HikariDataSource();
    private final CarparkTable carparkTable;
    private final VehicleTable vehicleTable;
    private final BusTable busTable;
    private final ApiKeyTable apiKeyTable;
    private final UserTable userTable;
    private final LogTable logTable;
    private final FoiTable foiTable;

    public MySQLConnection() {
        this.connect();
        this.carparkTable = new CarparkTable(dataSource);
        this.vehicleTable = new VehicleTable(dataSource);
        this.busTable = new BusTable(dataSource);
        this.apiKeyTable = new ApiKeyTable(dataSource);
        this.userTable = new UserTable(dataSource);
        this.logTable = new LogTable(dataSource);
        this.foiTable = new FoiTable(dataSource);
    }

    private void connect() {
        dataSource.setJdbcUrl("jdbc:mysql://localhost/opendata");
        dataSource.setUsername("root");
        dataSource.setPassword("password123");
        dataSource.setMaxLifetime(1800000);
    }
}
