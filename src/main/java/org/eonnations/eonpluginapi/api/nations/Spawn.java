package org.eonnations.eonpluginapi.api.nations;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Spawn extends Location {
    public Spawn(String worldName, double x, double y, double z) {
        super(Bukkit.getWorld(worldName), x, y, z);
    }

    public boolean teleport(Player player) {
        return player.teleport(this, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }
}
