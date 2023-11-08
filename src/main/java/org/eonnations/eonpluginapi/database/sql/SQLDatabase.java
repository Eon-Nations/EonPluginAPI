package org.eonnations.eonpluginapi.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.bukkit.Bukkit;
import org.eonnations.eonpluginapi.api.database.Database;
import org.eonnations.eonpluginapi.api.records.*;

import java.sql.*;
import java.util.UUID;

public class SQLDatabase implements Database {
    private final HikariDataSource dataSource;

    public SQLDatabase(Credentials credentials) {
        this.dataSource = setupDataSource(credentials);
    }

    private static HikariDataSource setupDataSource(Credentials credentials) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + credentials.url() + ":" + credentials.port() + "/" + credentials.database());
        config.setUsername(credentials.user());
        config.setPassword(credentials.password());
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(2000);
        return new HikariDataSource(config);
    }

    public String getUrl() {
        return dataSource.getJdbcUrl();
    }

    @Override
    public Either<SQLException, Vault> retrieveVault(int vaultId) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement prodCall = conn.prepareCall("CALL get_vault(?)")) {
                prodCall.setInt(1, vaultId);
                ResultSet set = prodCall.executeQuery();
                if (set.next()) {
                    Vault vault = parseVault(set, Option.of(vaultId));
                    return Either.right(vault);
                }
            }
            return Either.left(new SQLException("No vault found", "99001"));
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    @Override
    public Option<SQLException> removeVault(int vaultId) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement prodCall = conn.prepareCall("CALL remove_vault(?)")) {
                prodCall.setInt(1, vaultId);
                int updated = prodCall.executeUpdate();
                if (updated == 0) {
                    return Option.of(new SQLException("No vault to remove", "99001"));
                }
            }
            return Option.none();
        } catch (SQLException e) {
            return Option.of(e);
        }
    }

    @Override
    public Either<SQLException, Integer> createSpawn(int x, int y, int z) {
        return createSpawn(x, y, z, 0, 0);
    }

    @Override
    public Either<SQLException, Integer> createSpawn(int x, int y, int z, float yaw, float pitch) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement prodCall = conn.prepareCall("CALL create_spawn(?, ?, ?, ?, ?, ?)")) {
                prodCall.setInt(1, x);
                prodCall.setInt(2, y);
                prodCall.setInt(3, z);
                prodCall.setFloat(4, yaw);
                prodCall.setFloat(5, pitch);
                prodCall.registerOutParameter(6, Types.INTEGER);
                prodCall.executeQuery();
                int spawnId = prodCall.getInt(6);
                return Either.right(spawnId);
            }
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    @Override
    public Option<SQLException> createPlayer(UUID uuid, String username) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement prodCall = conn.prepareCall("CALL create_new_player(?, ?)")) {
                prodCall.setString(1, uuid.toString());
                prodCall.setString(2, username);
                int updated = prodCall.executeUpdate();
                if (updated == 0) {
                    return Option.of(new SQLException("Player created unsuccessfully", "99001"));
                }
                return Option.none();
            }
        } catch (SQLException e) {
            return Option.of(e);
        }
    }

    @Override
    public Either<SQLException, Integer> levelUpPlayer(UUID uuid) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement prodCall = conn.prepareCall("CALL level_up_player(?, ?)")) {
                prodCall.setString(1, uuid.toString());
                prodCall.registerOutParameter(2, Types.INTEGER);
                int updated = prodCall.executeUpdate();
                if (updated == 0) {
                    return Either.left(new SQLException("Player does not exist", "99001"));
                }
                return Either.right(prodCall.getInt(2));
            }
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    @Override
    public Either<SQLException, Vault> playerVault(UUID uuid) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement prodCall = conn.prepareCall("CALL player_vault(?)")) {
                prodCall.setString(1, uuid.toString());
                ResultSet result = prodCall.executeQuery();
                if (result.next()) {
                    int coins = result.getInt(1);
                    int iron = result.getInt(2);
                    int gold = result.getInt(3);
                    int diamonds = result.getInt(4);
                    int emeralds = result.getInt(5);
                    int vaultId = result.getInt(6);
                    return Either.right(new Vault(vaultId, coins, iron, gold, diamonds, emeralds));
                }
                return Either.left(new SQLException("Could not find vault", "99001"));
            }
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    public Vault parseVault(ResultSet set, Option<Integer> maybeVaultId) throws SQLException {
        int vaultId = maybeVaultId.getOrElse(set.getInt("vault_id"));
        int coins = set.getInt("coins");
        int iron = set.getInt("iron");
        int gold = set.getInt("gold");
        int diamonds = set.getInt("diamonds");
        int emeralds = set.getInt("emeralds");
        return new Vault(vaultId, coins, iron, gold, diamonds, emeralds);
    }

    @Override
    public Either<SQLException, EonPlayer> retrievePlayer(UUID uuid) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement prodCall = conn.prepareCall("CALL retrieve_player(?)")) {
                prodCall.setString(1, uuid.toString());
                ResultSet result = prodCall.executeQuery();
                if (result.next()) {
                    String donorRank = result.getString("donor_rank");
                    int level = result.getInt("level");
                    Option<String> townName = result.wasNull() ? Option.none() : Option.of(result.getString("town_name"));
                    Vault vault = parseVault(result, Option.none());
                    return Either.right(new EonPlayer(uuid, level, donorRank, townName, vault));
                }
                return Either.left(new SQLException("Player does not exist", "99001"));
            }
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    @Override
    public Either<SQLException, EonPlayer> retrievePlayer(String username) {
        return Option.of(Bukkit.getPlayerUniqueId(username))
                .map(this::retrievePlayer)
                .getOrElse(Either.left(new SQLException("Player UUID not valid", "99001")));
    }

    @Override
    public Either<SQLException, Vault> createTown(String name, UUID uuid, Spawn spawn) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement statement = conn.prepareCall("CALL create_full_town(?, ?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, name);
                statement.setString(2, uuid.toString());
                statement.setInt(3, spawn.getBlockX());
                statement.setInt(4, spawn.getBlockY());
                statement.setInt(5, spawn.getBlockZ());
                statement.setFloat(6, spawn.getYaw());
                statement.setFloat(7, spawn.getPitch());
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    Vault vault = parseVault(result, Option.none());
                    return Either.right(vault);
                }
                return Either.left(new SQLException("Town not created", "99001"));
            }
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    private Spawn parseSpawn(ResultSet set) throws SQLException {
        int x = set.getInt("x");
        int y = set.getInt("y");
        int z = set.getInt("z");
        float yaw = set.getFloat("yaw");
        float pitch = set.getFloat("pitch");
        return new Spawn(x, y, z, yaw, pitch);
    }

    @Override
    public Either<SQLException, Town> retrieveTown(String name) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement statement = conn.prepareCall("CALL retrieve_town(?)")) {
                statement.setString(1, name);
                ResultSet set = statement.executeQuery();
                if (set.next()) {
                    UUID owner = UUID.fromString(set.getString("owner"));
                    Vault vault = parseVault(set, Option.none());
                    Spawn spawn = parseSpawn(set);
                    return Either.right(new Town(name, owner, vault, spawn));
                }
                return Either.left(new SQLException("Unable to locate town", "99001"));
            }
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    @Override
    public Option<SQLException> changeTownSpawn(String name, Spawn newSpawn) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement statement = conn.prepareCall("CALL change_town_spawn(?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, name);
                statement.setInt(2, newSpawn.getBlockX());
                statement.setInt(3, newSpawn.getBlockY());
                statement.setInt(4, newSpawn.getBlockZ());
                statement.setFloat(5, newSpawn.getYaw());
                statement.setFloat(6, newSpawn.getPitch());
                int updated = statement.executeUpdate();
                if (updated == 0) {
                    return Option.of(new SQLException("Town not updated", "99001"));
                }
                return Option.none();
            }
        } catch (SQLException e) {
            return Option.of(e);
        }
    }

    @Override
    public boolean deleteTown(String name) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement prodCall = conn.prepareCall("CALL remove_town(?)")) {
                prodCall.setString(1, name);
                return prodCall.executeUpdate() != 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Either<SQLException, Integer> levelUpTown(String name) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement statement = conn.prepareCall("CALL level_up_town(?, ?)")) {
                statement.setString(1, name);
                statement.registerOutParameter(2, Types.INTEGER);
                int updated = statement.executeUpdate();
                if (updated == 0) {
                    return Either.left(new SQLException("Town does not exist", "99001"));
                }
                int level = statement.getInt("new_level");
                return Either.right(level);
            }
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    @Override
    public Option<SQLException> createNation(String name, String ownerTown) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement statement = conn.prepareCall("CALL create_nation(?, ?)")) {
                statement.setString(1, name);
                statement.setString(2, ownerTown);
                if (statement.executeUpdate() == 0) {
                    return Option.of(new SQLException("Nation not created", "99001"));
                }
                return Option.none();
            }
        } catch (SQLException e) {
            return Option.of(e);
        }
    }

    @Override
    public boolean deleteNation(String name) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement statement = conn.prepareCall("CALL remove_nation(?)")) {
                statement.setString(1, name);
                return statement.executeUpdate() != 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Either<SQLException, Nation> retrieveNation(String name) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement statement = conn.prepareCall("CALL retrieve_nation(?)")) {
                statement.setString(1, name);
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    Vault vault = parseVault(result, Option.none());
                    int level = result.getInt("level");
                    String ownerTown = result.getString("owner_town");
                    return Either.right(new Nation(name, ownerTown, level, vault));
                }
                return Either.left(new SQLException("Nation not found", "99001"));
            }
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    @Override
    public Either<SQLException, Integer> levelUpNation(String name) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement statement = conn.prepareCall("CALL level_up_nation(?, ?)")) {
                statement.setString(1, name);
                statement.registerOutParameter(2, Types.INTEGER);
                int updated = statement.executeUpdate();
                if (updated == 0) {
                    return Either.left(new SQLException("Nation does not exist", "99001"));
                }
                int level = statement.getInt("out_level");
                return Either.right(level);
            }
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    @Override
    public boolean addTownToNation(String nation, String town) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement statement = conn.prepareCall("CALL add_town_to_nation(?, ?)")) {
                statement.setString(1, town);
                statement.setString(2, nation);
                return statement.executeUpdate() != 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean removeTownFromNation(String town) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement statement = conn.prepareCall("CALL remove_town_from_nation(?)")) {
                statement.setString(1, town);
                return statement.executeUpdate() != 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Option<SQLException> addVote(UUID uuid, String website) {
        try (Connection conn = dataSource.getConnection()) {
            try (CallableStatement statement = conn.prepareCall("CALL new_vote(?, ?)")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, website);
                int updated = statement.executeUpdate();
                if (updated == 0) {
                    return Option.of(new SQLException("Vote addition has failed", "99001"));
                }
                return Option.none();
            }
        } catch (SQLException e) {
            return Option.of(e);
        }
    }
}
