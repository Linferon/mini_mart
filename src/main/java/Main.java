import migration.LiquibaseMigrator;
import util.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        try {
            LiquibaseMigrator.migrate();

        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}