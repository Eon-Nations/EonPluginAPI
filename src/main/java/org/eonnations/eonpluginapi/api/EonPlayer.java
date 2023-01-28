package org.eonnations.eonpluginapi.api;

import org.bukkit.entity.Player;

public interface EonPlayer {
    Player bukkitPlayer();
    double balance();
    int votes();
}
