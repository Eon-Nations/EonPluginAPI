package org.eonnations.eonpluginapi.api.database;

import org.eonnations.eonpluginapi.api.EonPlayer;
import org.eonnations.eonpluginapi.api.nations.Nation;
import org.eonnations.eonpluginapi.api.nations.Town;

import java.util.UUID;

public interface Database {
    EonPlayer retrievePlayer(UUID uuid);

    Town town(String name);

    Nation nation(String name);

    int balance(UUID uuid);

    int votes(UUID uuid);

}
