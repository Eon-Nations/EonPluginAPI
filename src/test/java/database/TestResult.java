package database;

import org.eonnations.eonpluginapi.database.dao.DatabaseVault;
import org.eonnations.eonpluginapi.database.queries.TableQuery;
import org.eonnations.eonpluginapi.database.sql.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestResult extends MySQLTestContainer {

    @BeforeEach
    void setup() throws SQLException {
        String QUERY = "INSERT INTO Vaults () VALUES ();";
        sendNoReturn(TableQuery.CREATE_VAULTS_TABLE);
        sendNoReturn(QUERY);
    }

    @Test
    @DisplayName("Using the DatabaseVault class, set all fields appropriately")
    void testResultVault() throws SQLException, ReflectiveOperationException {
        String QUERY = "SELECT * FROM Vaults";
        Result<DatabaseVault> result = sendRequest(QUERY, DatabaseVault.class);
        Optional<DatabaseVault> optVault = result.results().findAny();
        assertTrue(optVault.isPresent());
        DatabaseVault vault = optVault.get();
        assertTrue(vault.valid());
        assertEquals(0, vault.coins());
        assertEquals(0, vault.diamonds());
        assertEquals(0, vault.gold());
        assertEquals(0, vault.emeralds());
        assertEquals(0, vault.iron());
    }
}
