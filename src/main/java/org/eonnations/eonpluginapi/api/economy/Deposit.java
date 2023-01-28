package org.eonnations.eonpluginapi.api.economy;

import org.eonnations.eonpluginapi.api.Status;

import java.util.UUID;

@FunctionalInterface
public interface Deposit {
    Status deposit(UUID uuid, double amount);
}
