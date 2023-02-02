package org.eonnations.eonpluginapi.api.nations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Town {
    UUID owner();
    List<UUID> players();
    Spawn spawn();
    Optional<Nation> nation();
    int coins();
    // Add borders in
}
