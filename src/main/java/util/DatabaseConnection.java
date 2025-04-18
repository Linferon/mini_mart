package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private DatabaseConnection() {}
    private static final String URL = "jdbc:h2:./db/mini-mart;AUTO_SERVER=TRUE";
    private static final String USER = "root";
    private static final String PASSWORD = "qwerty";
    
    private static Connection connection;
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error closing database connection: " + e.getMessage());
        }
    }
}