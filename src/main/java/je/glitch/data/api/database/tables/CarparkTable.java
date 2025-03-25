package je.glitch.data.api.database.tables;

import com.zaxxer.hikari.HikariDataSource;
import je.glitch.data.api.models.Carpark;
import je.glitch.data.api.models.LiveParkingSpace;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CarparkTable implements ITable {
    private final HikariDataSource dataSource;

    /**
     * Returns all car parks in the database.
     * @return a list of car parks
     */
    public List<Carpark> getCarparks() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("""
                            SELECT 
                                carparks.*, 
                                companies.name AS ownerName, 
                                GROUP_CONCAT(carparkPaymentMethods.paymentMethod) AS paymentMethods 
                            FROM carparks 
                            LEFT JOIN companies ON companies.id = carparks.ownerId 
                            LEFT JOIN carparkPaymentMethods ON carparkPaymentMethods.carparkId = carparks.id 
                            GROUP BY carparks.id
                    """);

            try (ResultSet result = stmt.executeQuery()) {
                List<Carpark> carparks = new ArrayList<>();

                while (result.next()) {
                    carparks.add(Carpark.of(result));
                }
                return carparks;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    public Carpark getCarparkById(String id) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("""
                            SELECT 
                                carparks.*, 
                                companies.name AS ownerName, 
                                GROUP_CONCAT(carparkPaymentMethods.paymentMethod) AS paymentMethods 
                            FROM carparks 
                            LEFT JOIN companies ON companies.id = carparks.ownerId 
                            LEFT JOIN carparkPaymentMethods ON carparkPaymentMethods.carparkId = carparks.id 
                            WHERE carparks.id = ?
                    """);

            stmt.setString(1, id);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return Carpark.of(result);
                }
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public Carpark getCarparkByLiveTrackingCode(String code) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("""
                            SELECT 
                                carparks.*, 
                                companies.name AS ownerName, 
                                GROUP_CONCAT(carparkPaymentMethods.paymentMethod) AS paymentMethods 
                            FROM carparks 
                            LEFT JOIN companies ON companies.id = carparks.ownerId 
                            LEFT JOIN carparkPaymentMethods ON carparkPaymentMethods.carparkId = carparks.id 
                            WHERE carparks.liveTrackingCode = ?
                            GROUP BY carparks.id
                    """);

            stmt.setString(1, code);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return Carpark.of(result);
                }
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public List<LiveParkingSpace> getLiveSpacesForDate(String date) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM liveParkingSpaces WHERE DATE(createdAt) = ? ORDER BY createdAt DESC");
            stmt.setString(1, date);

            try (ResultSet result = stmt.executeQuery()) {
                List<LiveParkingSpace> spaces = new ArrayList<>();

                while (result.next()) {
                    spaces.add(LiveParkingSpace.of(result));
                }
                return spaces;
            }
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public List<String> getLiveSpacesDates() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT DISTINCT DATE_FORMAT(createdAt, '%Y-%m-%d') AS date FROM liveParkingSpaces ORDER BY date DESC");

            try (ResultSet result = stmt.executeQuery()) {
                List<String> dates = new ArrayList<>();

                while (result.next()) {
                    dates.add(result.getString("date"));
                }
                return dates;
            }
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
}
