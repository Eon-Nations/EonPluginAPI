package org.eonnations.eonpluginapi.api.records;

import java.util.UUID;

public record Town(String name, UUID owner, Vault vault, Spawn spawn) {
}
