package je.glitch.data.api.database.tables;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApiKeyTable implements ITable {
    private final HikariDataSource dataSource = new HikariDataSource();

    
}
