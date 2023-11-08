package org.eonnations.eonpluginapi.api.records;

import io.vavr.control.Option;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.UUID;

public record EonPlayer(UUID uuid, int level, String donorRank, Option<String> townName, Vault vault) {

    Player bukkitPlayer(Server server) {
        return server.getPlayer(uuid);
    }

    Player bukkitPlayer(Server server, UUID uuid) {
        return server.getPlayer(uuid);
    }
}
