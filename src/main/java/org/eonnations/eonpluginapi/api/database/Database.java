package org.eonnations.eonpluginapi.api.database;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.eonnations.eonpluginapi.api.records.*;

import java.sql.SQLException;
import java.util.UUID;

public interface Database {
    Either<SQLException, Vault> retrieveVault(int vaultId);
    Option<SQLException> removeVault(int vaultId);

    Either<SQLException, Integer> createSpawn(int x, int y, int z);
    Either<SQLException, Integer> createSpawn(int x, int y, int z, float yaw, float pitch);

    Option<SQLException> createPlayer(UUID uuid, String username);
    Either<SQLException, Integer> levelUpPlayer(UUID uuid);
    Either<SQLException, Vault> playerVault(UUID uuid);
    Either<SQLException, EonPlayer> retrievePlayer(UUID uuid);
    Either<SQLException, EonPlayer> retrievePlayer(String username);

    Either<SQLException, Vault> createTown(String name, UUID uuid, Spawn spawn);
    Either<SQLException, Town> retrieveTown(String name);
    Option<SQLException> changeTownSpawn(String name, Spawn newSpawn);
    boolean deleteTown(String name);
    Either<SQLException, Integer> levelUpTown(String name);

    Option<SQLException> createNation(String name, String ownerTown);
    boolean deleteNation(String name);
    Either<SQLException, Nation> retrieveNation(String name);
    Either<SQLException, Integer> levelUpNation(String name);
    boolean addTownToNation(String nation, String town);
    boolean removeTownFromNation(String town);

    Option<SQLException> addVote(UUID uuid, String website);
}
