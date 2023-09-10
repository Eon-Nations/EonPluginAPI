package database;

import org.eonnations.eonpluginapi.database.Credentials;
import org.eonnations.eonpluginapi.database.sql.Result;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;

@Testcontainers
public class MySQLTestContainer {

    @Container
    protected static GenericContainer mysql = new GenericContainer("mysql:8.1")
            .withExposedPorts(3306)
            .withEnv("MYSQL_ROOT_PASSWORD", "root_password")
            .withEnv("MYSQL_DATABASE", "server");

    protected Credentials credentials;

    @BeforeEach
    void beforeAll() {
        String mappedPort = mysql.getMappedPort(3306).toString();
        credentials = new Credentials(mysql.getHost(), mappedPort, "root", "root_password", "server");
    }

    protected <T> Result<T> sendRequest(String query, Class<T> resultClass, Object... parameters) throws SQLException, ReflectiveOperationException {
        String url = "jdbc:mysql://" + credentials.url() + ":" + credentials.port() + "/" + credentials.database();
        try (Connection conn = DriverManager.getConnection(url, credentials.user(), credentials.password())) {
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i, parameters[i]);
                }
                ResultSet set = statement.executeQuery();
                return Result.makeResultsFromSet(set, resultClass);
            }
        }
    }

    protected void sendNoReturn(String query, Object... parameters) throws SQLException {
        String url = "jdbc:mysql://" + credentials.url() + ":" + credentials.port() + "/" + credentials.database();
        try (Connection conn = DriverManager.getConnection(url, credentials.user(), credentials.password())) {
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i, parameters[i]);
                }
                statement.execute();
            }
        }
    }
}
