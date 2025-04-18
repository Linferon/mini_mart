package migration;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;

import util.DatabaseConnection;
import util.LoggerUtil;

public class LiquibaseMigrator {
    private LiquibaseMigrator(){}

    public static void migrate() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    "src/main/resources/db/changelog/master.yaml",
                    new ClassLoaderResourceAccessor(),
                    database);

            liquibase.update(new Contexts(), new LabelExpression());
            LoggerUtil.info("Database migration completed successfully");
        } catch (Exception e) {
            LoggerUtil.error("Error during database migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}