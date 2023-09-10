package org.eonnations.eonpluginapi.api.database;

import org.eonnations.eonpluginapi.api.EonPlayer;
import org.eonnations.eonpluginapi.api.economy.Vault;
import org.eonnations.eonpluginapi.api.economy.Vote;
import org.eonnations.eonpluginapi.api.nations.Nation;
import org.eonnations.eonpluginapi.api.nations.Spawn;
import org.eonnations.eonpluginapi.api.nations.Town;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Database {
    Vault townVault(String townName);
    Vault playerVault(UUID uuid);
    Vault playerVault(String uuidString);
    Vault createVault();

    Nation createNation(String name);
    Optional<Nation> nation(String name);
    boolean removeNation(String name);

    Town createTown(String name, UUID owner, Spawn spawn);
    Optional<Town> town(String name);
    boolean removeTown(String name);

    EonPlayer createPlayer(UUID uuid, String name);
    Optional<EonPlayer> retrievePlayer(UUID uuid);
    Optional<EonPlayer> retrievePlayer(String name);

    Vote createVote(UUID uuid);
    List<Vote> findVotes(UUID uuid);
}
