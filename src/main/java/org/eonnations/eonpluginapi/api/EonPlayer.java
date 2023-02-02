package org.eonnations.eonpluginapi.api;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.eonnations.eonpluginapi.api.nations.Town;

import java.util.UUID;

public interface EonPlayer {
    UUID uuid();
    Town town();
    int coins();
    int votes();

    default Player bukkitPlayer(Server server) {
        return server.getPlayer(uuid());
    }
}
