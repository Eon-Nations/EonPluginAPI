import database.MySQLTestContainer;
import org.bukkit.configuration.file.FileConfiguration;
import org.eonnations.eonpluginapi.EonPlugin;
import org.eonnations.eonpluginapi.database.sql.SQLClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSQLClient extends MySQLTestContainer {

    EonPlugin plugin;

    FileConfiguration config;

    UUID testUUID;

    SQLClient client;

    @BeforeEach
    void setup() {
        plugin = Mockito.mock(EonPlugin.class);
        config = Mockito.mock(FileConfiguration.class);
        client = SQLClient.setupClient(credentials);
        testUUID = UUID.randomUUID();
    }


    @Test
    @DisplayName("Test that all tables exist")
    void testSetup() throws SQLException {
        List<String> tables = List.of("Players", "Vaults", "PlayerTowns", "Towns", "Nations", "Votes", "Nodes", "Wars");
        Connection conn = DriverManager.getConnection(client.getUrl(), "root", "root_password");
        DatabaseMetaData metaData = conn.getMetaData();
        for (String table : tables) {
            ResultSet set = metaData.getTables(null, null, table, null);
            assertTrue(set.next());
        }
    }


    @Test
    void testRetrieveTown() {

    }
}
