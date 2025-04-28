package migration;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.DatabaseConnection;

import static util.LoggerUtil.error;

public class LiquibaseMigrator {
    private LiquibaseMigrator() {
    }

    public static void migrate() {
        Logger liquibaseLogger = Logger.getLogger("liquibase");
        liquibaseLogger.setLevel(Level.WARNING);

        try (Connection connection = DatabaseConnection.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    "db/changelog/master.yaml",
                    new ClassLoaderResourceAccessor(),
                    database);

            liquibase.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            error("Error during database migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}