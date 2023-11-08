package database.sql;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.eonnations.eonpluginapi.api.records.*;
import org.eonnations.eonpluginapi.database.sql.Credentials;
import org.eonnations.eonpluginapi.database.sql.SQLDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class TestSQLDatabase {
    @Container
    private static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:latest")
            .withUsername("root")
            .withPassword("root_password")
            .withDatabaseName("test_db")
            .withExposedPorts(3306)
            .withCopyFileToContainer(MountableFile.forClasspathResource("tables_functions.sql"), "/docker-entrypoint-initdb.d/tables_functions.sql");

    private SQLDatabase sqlDatabase;
    private static final NameGenerator nameGen = new NameGenerator();
    private static final Spawn defaultSpawn = new Spawn(1, 1, 1, 1, 1);

    @BeforeEach
    void setup() {
        Credentials credentials = new Credentials(mysql.getHost(), String.valueOf(mysql.getMappedPort(3306)), "root", "root_password", "test_db");
        sqlDatabase = new SQLDatabase(credentials);
    }

    private Vault createVault() throws SQLException {
        int vaultId = -1;
        try (Connection conn = DriverManager.getConnection(sqlDatabase.getUrl(), "root", "root_password")) {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO vaults (coins, iron, gold, diamonds, emeralds) VALUES (10, 5, 2, 1, 15)", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.executeUpdate();
            ResultSet set = statement.getGeneratedKeys();
            if (set.next()) {
                vaultId = set.getInt(1);
            }
        }
        return new Vault(vaultId, 10, 5, 2, 1, 15);
    }

    @Test
    @DisplayName("Retrieving an invalid vault returns a SQLException")
    void testInvalidRetrieve() {
        Either<SQLException, Vault> vaultResult = sqlDatabase.retrieveVault(-2);
        SQLException e = vaultResult.getLeft();
        assertEquals("99001", e.getSQLState());
    }

    @Test
    @DisplayName("Retrieving an existing vault returns the Vault")
    void testValidRetrieve() throws SQLException {
        int vaultId = createVault().id();
        Either<SQLException, Vault> vaultResult = sqlDatabase.retrieveVault(vaultId);
        assertTrue(vaultResult.isRight());
        Vault vault = vaultResult.get();
        assertEquals(10, vault.coins());
        assertEquals(5, vault.iron());
        assertEquals(2, vault.gold());
        assertEquals(1, vault.diamonds());
        assertEquals(15, vault.emeralds());
    }

    @Test
    @DisplayName("Removing a non-existent vault throws an error")
    void testNoVaultRemove() {
        Option<SQLException> error = sqlDatabase.removeVault(-5);
        assertFalse(error.isEmpty());
        assertEquals("99001", error.get().getSQLState());
    }

    @Test
    @DisplayName("Removing an existing vault actually removes it from the database")
    void testExistingVaultRemove() throws SQLException {
        Vault createdVault = createVault();
        Option<SQLException> noError = sqlDatabase.removeVault(createdVault.id());
        assertTrue(noError.isEmpty());
        Either<SQLException, Vault> shouldBeError = sqlDatabase.retrieveVault(createdVault.id());
        assertTrue(shouldBeError.isLeft());
    }

    @Test
    @DisplayName("Creating a spawn creates a spawn")
    void testCreateSpawn() {
        Either<SQLException, Integer> spawnResult = sqlDatabase.createSpawn(10, 10, 10, 5.0F, 5.0F);
        assertTrue(spawnResult.isRight());
        assertTrue(spawnResult.get() > 0);
    }

    @Test
    @DisplayName("Creating two different spawns yields different ids")
    void testTwoSpawnsCreate() {
        Either<SQLException, Integer> firstSpawn = sqlDatabase.createSpawn(10, 10, 10, 5.0F, 5.0F);
        Either<SQLException, Integer> secondSpawn = sqlDatabase.createSpawn(20, 20, 20, 10.0F, 10.0F);
        assertTrue(firstSpawn.isRight());
        assertTrue(secondSpawn.isRight());
        assertNotEquals(firstSpawn.get(), secondSpawn.get());
        assertEquals(firstSpawn.get() + 1, secondSpawn.get());
    }

    @Test
    @DisplayName("Creating a player creates a player")
    void testCreatePlayer() {
        UUID uuid = UUID.randomUUID();
        Option<SQLException> error = sqlDatabase.createPlayer(uuid, nameGen.name());
        assertTrue(error.isEmpty());
    }

    @Test
    @DisplayName("Leveling up a non-existent player throws an error")
    void testNoPlayerLevelUp() {
        UUID uuid = UUID.randomUUID();
        Either<SQLException, Integer> levelResult = sqlDatabase.levelUpPlayer(uuid);
        assertTrue(levelResult.isLeft());
        SQLException e = levelResult.getLeft();
        assertEquals("99001", e.getSQLState());
    }

    @Test
    @DisplayName("Leveling a player levels by only one level")
    void testPlayerLevelUp() throws Exception {
        UUID uuid = UUID.randomUUID();
        Option<SQLException> error = sqlDatabase.createPlayer(uuid, nameGen.name())
                .peek(SQLException::printStackTrace);
        assertTrue(error.isEmpty());
        Either<SQLException, Integer> levelResult = sqlDatabase.levelUpPlayer(uuid);
        assertTrue(levelResult.isRight());
        assertEquals(2, levelResult.getOrElseThrow(() -> new Exception("That didn't work")));
    }

    @Test
    @DisplayName("Grabbing a non-existent player's vault doesn't work")
    void testNoPlayerVaultGrab() {
        UUID uuid = UUID.randomUUID();
        Either<SQLException, Vault> vaultResult = sqlDatabase.playerVault(uuid);
        assertTrue(vaultResult.isLeft());
        SQLException e = vaultResult.getLeft();
        assertEquals("99001", e.getSQLState());
    }

    @Test
    @DisplayName("Grabbing an existing player's vault works")
    void testPlayerVaultGrab() {
        UUID uuid = UUID.randomUUID();
        sqlDatabase.createPlayer(uuid, nameGen.name())
                .peek(SQLException::printStackTrace);
        Either<SQLException, Vault> vaultResult = sqlDatabase.playerVault(uuid)
                .peekLeft(SQLException::printStackTrace);
        assertTrue(vaultResult.isRight());
        Vault vault = vaultResult.get();
        assertTrue(vault.id() > 0);
        assertEquals(0, vault.coins());
    }

    @Test
    @DisplayName("Grabbing a non-existent player from table throws error")
    void testNonExistentPlayer() {
        UUID uuid = UUID.randomUUID();
        Either<SQLException, EonPlayer> playerResult = sqlDatabase.retrievePlayer(uuid);
        assertTrue(playerResult.isLeft());
        SQLException e = playerResult.getLeft();
        assertEquals("99001", e.getSQLState());
    }

    @Test
    @DisplayName("Grabbing an existing player from table returns correct player")
    void testExistingPlayer() {
        UUID uuid = UUID.randomUUID();
        sqlDatabase.createPlayer(uuid, nameGen.name());
        Either<SQLException, EonPlayer> playerResult = sqlDatabase.retrievePlayer(uuid);
        assertTrue(playerResult.isRight());
        assertEquals(uuid.toString(), playerResult.get().uuid().toString());
    }

    @Test
    @DisplayName("Grabbing a player by username works just as well")
    void testExistingPlayerUsername() {
        Server server = mock(Server.class);
        when(server.getLogger()).thenReturn(Logger.getLogger("Server"));
        Bukkit.setServer(server);
        UUID uuid = UUID.randomUUID();
        when(server.getPlayerUniqueId(anyString())).thenReturn(uuid);
        String username = nameGen.name();
        sqlDatabase.createPlayer(uuid, username);
        Either<SQLException, EonPlayer> playerResult = sqlDatabase.retrievePlayer(username);
        assertTrue(playerResult.isRight());
    }

    @Test
    @DisplayName("Creating a town updates the player as well")
    void testTownUpdatePlayer() throws Exception {
        UUID uuid = UUID.randomUUID();
        String townName = nameGen.name();
        sqlDatabase.createPlayer(uuid, nameGen.name());
        Either<SQLException, Vault> townResult = sqlDatabase.createTown(townName, uuid, new Spawn(1, 1, 1, 1, 1));
        assertTrue(townResult.isRight());
        Either<SQLException, EonPlayer> playerResult = sqlDatabase.retrievePlayer(uuid);
        assertTrue(playerResult.isRight());
        EonPlayer result = playerResult.getOrElseThrow(() -> new Exception("No player"));
        assertFalse(result.townName().isEmpty());
        assertEquals(townName, result.townName().get());
    }

    @Test
    @DisplayName("Creating a town with the same name returns an error")
    void testDuplicateTown() {
        UUID player1 = UUID.randomUUID();
        sqlDatabase.createPlayer(player1, nameGen.name());
        UUID player2 = UUID.randomUUID();
        sqlDatabase.createPlayer(player2, nameGen.name());
        Either<SQLException, Vault> firstTown = sqlDatabase.createTown("Duplicate", player1, new Spawn(1, 1, 1, 1, 1));
        assertTrue(firstTown.isRight());
        Either<SQLException, Vault> secondTown = sqlDatabase.createTown("Duplicate", player2, new Spawn(1, 1, 1, 1, 1));
        assertTrue(secondTown.isLeft());
    }

    @Test
    @DisplayName("Changing a town's spawn that doesn't exist throws an error")
    void testNonExistentTownSpawn() {
        Option<SQLException> optExec = sqlDatabase.changeTownSpawn("Non-Existent-Town", new Spawn(2, 2, 2, 2, 2));
        assertFalse(optExec.isEmpty());
    }

    @Test
    @DisplayName("Retrieving a town that does not exist throws an error")
    void testNonExistentTownRetrieve() {
        Either<SQLException, Town> townResult = sqlDatabase.retrieveTown("Non-Existent-Town");
        assertTrue(townResult.isLeft());
    }

    @Test
    @DisplayName("Retrieving a town that exists returns its details")
    void testExistingTownRetrieve() {
        UUID uuid = UUID.randomUUID();
        sqlDatabase.createPlayer(uuid, nameGen.name());
        String townName = nameGen.name();
        Either<SQLException, Vault> createResult = sqlDatabase.createTown(townName, uuid, new Spawn(5, 5, 5, 5, 5))
                .peekLeft(SQLException::printStackTrace);
        assertTrue(createResult.isRight());
        Either<SQLException, Town> townResult = sqlDatabase.retrieveTown(townName)
                .peekLeft(SQLException::printStackTrace);
        assertTrue(townResult.isRight());
        Town town = townResult.get();
        assertEquals(uuid, town.owner());
        assertEquals(townName, town.name());
        assertEquals(5, town.spawn().getBlockX());
    }

    @Test
    @DisplayName("Changing an existing town's spawn successfully changes")
    void testExistingTownSpawn() {
        UUID uuid = UUID.randomUUID();
        String townName = nameGen.name();
        sqlDatabase.createPlayer(uuid, nameGen.name());
        Either<SQLException, Vault> townResult = sqlDatabase.createTown(townName, uuid, new Spawn(1, 1, 1, 1, 1));
        assertTrue(townResult.isRight());
        Option<SQLException> optExec = sqlDatabase.changeTownSpawn(townName, new Spawn(2, 2, 2, 2, 2))
                .peek(SQLException::printStackTrace);
        assertTrue(optExec.isEmpty());
    }

    @Test
    @DisplayName("Deleting a town that does not exist does nothing")
    void testNonExistentTownDelete() {
        boolean deleted = sqlDatabase.deleteTown("Non-Existent-Town");
        assertFalse(deleted);
        Either<SQLException, Town> townResult = sqlDatabase.retrieveTown("Non-Existent-Town");
        assertTrue(townResult.isLeft());
    }

    @Test
    @DisplayName("Leveling up a town that does not exist returns an exception")
    void testLevelUpNonExistentTown() {
        Either<SQLException, Integer> levelResult = sqlDatabase.levelUpTown("Non-Existent-Town");
        assertTrue(levelResult.isLeft());
    }

    @Test
    @DisplayName("Leveling up a town increases it only by 1")
    void testLevelUpExistingTown() {
        UUID uuid = UUID.randomUUID();
        sqlDatabase.createPlayer(uuid, nameGen.name());
        String townName = nameGen.name();
        sqlDatabase.createTown(townName, uuid, new Spawn(1, 1, 1, 1, 1));
        Either<SQLException, Integer> levelResult = sqlDatabase.levelUpTown(townName)
                .peekLeft(SQLException::printStackTrace);
        assertTrue(levelResult.isRight());
        int level = levelResult.getOrElse(-1);
        assertNotEquals(-1, level);
        assertEquals(2, level);
    }

    private void createTown(UUID uuid, String playerName, String townName) {
        Option<SQLException> optPlayer = sqlDatabase.createPlayer(uuid, playerName);
        assertTrue(optPlayer.isEmpty());
        Either<SQLException, Vault> optTown = sqlDatabase.createTown(townName, uuid, defaultSpawn);
        assertTrue(optTown.isRight());
    }

    private void createNation(UUID uuid, String playerName, String ownerTown, String nationName) {
        createTown(uuid, playerName, ownerTown);
        Option<SQLException> optNation = sqlDatabase.createNation(nationName, ownerTown);
        assertTrue(optNation.isEmpty());
    }

    @Test
    @DisplayName("Creating a nation throws no error")
    void testCreatingNation() {
        createNation(UUID.randomUUID(), nameGen.name(), nameGen.name(), nameGen.name());
    }

    @Test
    @DisplayName("Creating a nation persists beyond the initial creation")
    void testCreatingNationPersist() {
        String nationName = nameGen.name();
        String ownerTown = nameGen.name();
        UUID uuid = UUID.randomUUID();
        createNation(uuid, nationName, ownerTown, nationName);
        Either<SQLException, Nation> nationResult = sqlDatabase.retrieveNation(nationName)
                .peekLeft(SQLException::printStackTrace);
        assertTrue(nationResult.isRight());
        assertEquals(nationName, nationResult.get().name());
        assertEquals(ownerTown, nationResult.get().ownerTown());

    }

    @Test
    @DisplayName("Deleting a nation that does not exist does not affect the program")
    void testDeletingNonExistent() {
        boolean deleted = sqlDatabase.deleteNation("Non_Existent_Nation");
        assertFalse(deleted);
    }

    @Test
    @DisplayName("Deleting a nation that does exist deletes the nation in the database")
    void testDeletingExistingNation() {
        UUID uuid = UUID.randomUUID();
        String ownerTown = nameGen.name();
        String nationName = nameGen.name();
        createNation(uuid, nameGen.name(), ownerTown, nationName);
        boolean deleted = sqlDatabase.deleteNation(nationName);
        assertTrue(deleted);
    }

    @Test
    @DisplayName("Leveling up a non existent nation returns an error")
    void testNonExistentLevelUpNation() {
        Either<SQLException, Integer> levelResult = sqlDatabase.levelUpNation("Non_Existing_Nation");
        assertTrue(levelResult.isLeft());
    }

    @Test
    @DisplayName("Leveling up an existing nation increases their level by 1")
    void testExistingLevelUpNation() {
        UUID uuid = UUID.randomUUID();
        String ownerTown = nameGen.name();
        String nationName = nameGen.name();
        createNation(uuid, nameGen.name(), ownerTown, nationName);
        Either<SQLException, Integer> levelResult = sqlDatabase.levelUpNation(nationName);
        assertTrue(levelResult.isRight());
        int level = levelResult.getOrElse(0);
        assertEquals(2, level);
    }

    @Test
    @DisplayName("Adding a non-existing town to a nation does not work")
    void testNonExistingTownToNation() {
        UUID uuid = UUID.randomUUID();
        String ownerTown = nameGen.name();
        String nationName = nameGen.name();
        createNation(uuid, nameGen.name(), ownerTown, nationName);
        boolean added = sqlDatabase.addTownToNation(nationName, "Non-Existing-Town");
        assertFalse(added);
    }

    @Test
    @DisplayName("Adding an existing town to a non-existing nation does not work")
    void testNonExistingNationAdd() {
        UUID uuid = UUID.randomUUID();
        String ownerTown = nameGen.name();
        createTown(uuid, nameGen.name(), ownerTown);
        boolean added = sqlDatabase.addTownToNation("non-existent-nation", ownerTown);
        assertFalse(added);
    }

    @Test
    @DisplayName("Adding an existing town to an existing nation works")
    void testExistingTownToNation() {
        String anotherTown = nameGen.name();
        String nationName = nameGen.name();
        createNation(UUID.randomUUID(), nameGen.name(), nameGen.name(), nationName);
        createTown(UUID.randomUUID(), nameGen.name(), anotherTown);
        boolean added = sqlDatabase.addTownToNation(nationName, anotherTown);
        assertTrue(added);
    }

    @Test
    @DisplayName("Removing town from non-existent nation does not work")
    void testRemovingNonExistentTown() {
        String townName = nameGen.name();
        createTown(UUID.randomUUID(), nameGen.name(), townName);
        boolean removed = sqlDatabase.removeTownFromNation(townName);
        assertFalse(removed);
    }

    @Test
    @DisplayName("Removing non-existent town from nation does not work")
    void testRemovingNonExistentTownFromNation() {
        String nationName = nameGen.name();
        String ownerTown = nameGen.name();
        createNation(UUID.randomUUID(), nameGen.name(), ownerTown, nationName);
        boolean removed = sqlDatabase.removeTownFromNation("No-Nation");
        assertFalse(removed);
    }

    @Test
    @DisplayName("Removing town from nation works")
    void testRemoveTownFromNation() {
        String nationName = nameGen.name();
        String memberTown = nameGen.name();
        createNation(UUID.randomUUID(), nameGen.name(), nameGen.name(), nationName);
        createTown(UUID.randomUUID(), nameGen.name(), memberTown);
        sqlDatabase.addTownToNation(nationName, memberTown);
        boolean removed = sqlDatabase.removeTownFromNation(memberTown);
        assertTrue(removed);
    }
}
