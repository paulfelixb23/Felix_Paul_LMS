/**
 * Paul Felix
 * CEN-3024 - Software Development
 * 01/26/2024
 * DatabaseConnection.java
 * This class severs the purpose of creating the connection from the SQLite database to our LMS application.
 */

package libraryms.libraryms;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:LibraryMS_DB.db";
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
