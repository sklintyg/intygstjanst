package se.inera.certificate.liquibase;


import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class Runner {
    public static void main(String[] args) {
        String script = "changelog/changelog.xml";
        System.out.println(System.getProperty("java.class.path"));

        try {
            DatabaseConnection connection = new JdbcConnection(getConnection());
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            Liquibase liquibase = new Liquibase(script, new ClassLoaderResourceAccessor(), database);
            System.out.println(database.getConnection().getURL());
            liquibase.update(null);
            List<ChangeSet> changeSets = liquibase.listUnrunChangeSets(null);
            if (!changeSets.isEmpty()) {
                StringBuilder errors = new StringBuilder();
                for (ChangeSet changeSet : changeSets) {
                    errors.append('>').append(changeSet.toString()).append('\n');
                }
                throw new Error("Database version mismatch. Check liquibase status. Errors:\n" + errors.toString() + database.getDatabaseProductName() + ", " + database);
            }
        } catch (liquibase.exception.LiquibaseException | SQLException | ClassNotFoundException | IOException e) {
            throw new Error("Database not ok, aborting update.", e);
        }
        System.out.println("Done.");
    }

    private static Connection getConnection() throws IOException, ClassNotFoundException, SQLException {
        Properties properties = readProps();
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(url, username, password);
    }

    private static Properties readProps() throws IOException {
        Properties properties = new Properties();
        properties.load(Runner.class.getClassLoader().getResourceAsStream("runner.properties"));
        return properties;
    }
}
