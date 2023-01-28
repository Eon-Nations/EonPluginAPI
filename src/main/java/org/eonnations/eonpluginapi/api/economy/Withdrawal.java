package org.eonnations.eonpluginapi.api.economy;

import org.eonnations.eonpluginapi.api.Status;

import java.util.UUID;

public interface Withdrawal {
    Status withdraw(UUID uuid, double amount);
}
