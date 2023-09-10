package org.eonnations.eonpluginapi.api.nations;

import org.eonnations.eonpluginapi.api.database.Database;
import org.eonnations.eonpluginapi.api.economy.Vault;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Town {
    UUID owner();
    List<UUID> players();
    Spawn spawn();
    Optional<Nation> nation();
    Vault vault(Database database);

    static Town create(UUID owner, Spawn spawn) {
        return new Town() {
            @Override
            public UUID owner() {
                return owner;
            }

            @Override
            public List<UUID> players() {
                return List.of(owner);
            }

            @Override
            public Spawn spawn() {
                return spawn;
            }

            @Override
            public Optional<Nation> nation() {
                return Optional.empty();
            }

            @Override
            public Vault vault(Database database) {
                return database.createVault();
            }
        };
    }
}
