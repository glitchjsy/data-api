package je.glitch.data.api.database;

import com.zaxxer.hikari.HikariDataSource;
import je.glitch.data.api.database.tables.BusTable;
import je.glitch.data.api.database.tables.CarparkTable;
import je.glitch.data.api.database.tables.VehicleTable;
import lombok.Getter;

@Getter
public class MySQLConnection {
    private final HikariDataSource dataSource = new HikariDataSource();
    private final CarparkTable carparkTable;
    private final VehicleTable vehicleTable;
    private final BusTable busTable;

    public MySQLConnection() {
        this.connect();
        this.carparkTable = new CarparkTable(dataSource);
        this.vehicleTable = new VehicleTable(dataSource);
        this.busTable = new BusTable(dataSource);
    }

    private void connect() {
        dataSource.setJdbcUrl("jdbc:mysql://localhost/opendata");
        dataSource.setUsername("root");
        dataSource.setPassword("password123");
    }
}
