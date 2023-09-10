package org.eonnations.eonpluginapi.database.sql;

import org.eonnations.eonpluginapi.api.EonPlayer;
import org.eonnations.eonpluginapi.api.database.Database;
import org.eonnations.eonpluginapi.api.economy.Vault;
import org.eonnations.eonpluginapi.api.economy.Vote;
import org.eonnations.eonpluginapi.api.nations.Nation;
import org.eonnations.eonpluginapi.api.nations.Spawn;
import org.eonnations.eonpluginapi.api.nations.Town;
import org.eonnations.eonpluginapi.database.Credentials;
import org.eonnations.eonpluginapi.database.DatabaseNation;
import org.eonnations.eonpluginapi.database.DatabaseVault;
import org.eonnations.eonpluginapi.database.queries.TableQuery;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

public class SQLClient implements Database {
    private final String url;
    private final Properties props;


    private SQLClient(Credentials credentials) throws SQLException {
        this.url = "jdbc:mysql://" + credentials.url() + ":" + credentials.port() + "/" + credentials.database();
        props = new Properties();
        props.setProperty("user", credentials.user());
        props.setProperty("password", credentials.password());
        initializeDatabase();
    }

    public static SQLClient setupClient(Credentials credentials) {
        try {
            return new SQLClient(credentials);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUrl() {
        return url;
    }

    private void initializeDatabase() {
        String[] tableQueries = {
                TableQuery.CREATE_VAULTS_TABLE,
                TableQuery.CREATE_SPAWN_TABLE,
                TableQuery.CREATE_PLAYERS_TABLE,
                TableQuery.CREATE_NATIONS_TABLE,
                TableQuery.CREATE_TOWNS_TABLE,
                TableQuery.CREATE_TOWN_MEMBER_TABLE,
                TableQuery.CREATE_PLAYER_TOWN_TABLE,
                TableQuery.CREATE_VOTES_TABLE,
                TableQuery.CREATE_NODES_TABLE,
                TableQuery.CREATE_WARS_TABLE
        };
        Arrays.stream(tableQueries).forEachOrdered(this::sendNoReturn);
    }

    private void sendNoReturn(String query) {
        try (Connection conn = DriverManager.getConnection(url, props)) {
            Statement statement = conn.createStatement();
            statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendNoReturn(String query, Object... parameters) {
        try (Connection conn = DriverManager.getConnection(url, props)) {
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i, parameters[i]);
                }
                statement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Optional<ResultSet> sendRawQuery(String query) {
        try (Connection conn = DriverManager.getConnection(url, props)) {
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                return Optional.of(statement.executeQuery());
            }
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    private <T> Result<T> sendQuery(String query, Class<T> resultClass, Consumer<Exception> handler, Object... parameters) {
        try (Connection conn = DriverManager.getConnection(url, props)) {
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i, parameters[i]);
                }
                ResultSet resultSet = statement.executeQuery(query);
                return Result.makeResultsFromSet(resultSet, resultClass);
            }
        } catch (SQLException | ReflectiveOperationException e) {
            handler.accept(e);
            return Result.empty();
        }
    }

    @Override
    public Vault townVault(String townName) {
        String QUERY = """
                SELECT Towns.VaultID, Coins, Iron, Gold, Diamonds, Emeralds
                FROM Towns
                INNER JOIN Vaults
                ON Towns.VaultID = Vaults.VaultsID
                WHERE Towns.Name = ?;
                """;
        Result<DatabaseVault> result = sendQuery(QUERY, DatabaseVault.class, Exception::printStackTrace, townName);
        return result.results()
                .findFirst()
                .orElse(DatabaseVault.invalidVault());
    }

    @Override
    public Vault playerVault(UUID uuid) {
        String QUERY = """
                SELECT Players.VaultID, Coins, Iron, Gold, Diamonds, Emeralds
                FROM Players
                INNER JOIN Vaults
                ON Players.VaultID = Vaults.VaultsID
                WHERE UUID = ?;
                """;
        Result<DatabaseVault> result = sendQuery(QUERY, DatabaseVault.class, Exception::printStackTrace, uuid.toString());
        return result.results()
                .findFirst()
                .orElse(DatabaseVault.invalidVault());
    }

    @Override
    public Vault playerVault(String uuidString) {
        UUID uuid = UUID.fromString(uuidString);
        return playerVault(uuid);
    }

    @Override
    public Vault createVault() {
        String BANK_QUERY = "INSERT INTO Wallets () VALUES (); SELECT LAST_INSERT_ID();";
        Optional<ResultSet> resultSet = sendRawQuery(BANK_QUERY);
        if (resultSet.isPresent()) {
            ResultSet set = resultSet.get();
            try {
                int bankId = set.getInt("LAST_INSERT_ID()");
                return Vault.newVault(bankId, true);
            } catch (SQLException e) {
                return Vault.newVault(-1, false);
            }
        }
        return Vault.newVault(-1, false);
    }

    @Override
    public Nation createNation(String name) {
        Vault vault = createVault();
        String NATION_QUERY = "INSERT INTO Nations (Name, Level, VaultID) VALUES (?, ?, ?)";
        sendNoReturn(NATION_QUERY, name, 1, vault.id());
        return new DatabaseNation(List.of(), name, 1);
    }

    @Override
    public Optional<Nation> nation(String name) {
        String QUERY = "SELECT Owner, Level, VaultID FROM Nations WHERE Name = ?;";
        Result<DatabaseNation> nationResult = sendQuery(QUERY, DatabaseNation.class, Exception::printStackTrace, name);
        return nationResult.results().findFirst()
                .map(n -> n);
    }

    @Override
    public boolean removeNation(String name) {
        String DELETE = "DELETE FROM Nations WHERE Name = ?";
        sendNoReturn(DELETE, name);
        return true;
    }

    @Override
    public Town createTown(String name, UUID owner, Spawn spawn) {
        Vault vault = createVault();
        String QUERY = "INSERT INTO Towns (Name, Level, VaultID) VALUES (?, ?, ?)";
        sendNoReturn(QUERY, name, 1, vault.id());
        return new Town() {
            @Override
            public UUID owner() {
                return owner;
            }

            @Override
            public List<UUID> players() {
                return List.of(owner);
            }

            @Override
            public Spawn spawn() {
                return spawn;
            }

            @Override
            public Optional<Nation> nation() {
                return Optional.empty();
            }

            @Override
            public Vault vault(Database database) {
                return vault;
            }
        };
    }

    @Override
    public Optional<Town> town(String name) {
        String SELECT_QUERY = "SELECT Owner, VaultID, Spawn FROM Towns WHERE Name = ?;";

        return Optional.empty();
    }

    @Override
    public boolean removeTown(String name) {
        return false;
    }

    @Override
    public EonPlayer createPlayer(UUID uuid, String name) {
        return null;
    }

    @Override
    public Optional<EonPlayer> retrievePlayer(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<EonPlayer> retrievePlayer(String name) {
        return Optional.empty();
    }

    @Override
    public Vote createVote(UUID uuid) {
        return null;
    }

    @Override
    public List<Vote> findVotes(UUID uuid) {
        return null;
    }
}
