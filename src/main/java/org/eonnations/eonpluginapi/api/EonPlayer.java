package org.eonnations.eonpluginapi.api;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.eonnations.eonpluginapi.api.economy.Vault;
import org.eonnations.eonpluginapi.api.nations.Town;

import java.util.Optional;
import java.util.UUID;

public interface EonPlayer {
    UUID uuid();
    Optional<Town> town();
    String level();
    String donorRank();
    Vault vault();

    default Player bukkitPlayer(Server server) {
        return server.getPlayer(uuid());
    }

    default Player bukkitPlayer(Server server, UUID uuid) {
        return server.getPlayer(uuid);
    }
}
