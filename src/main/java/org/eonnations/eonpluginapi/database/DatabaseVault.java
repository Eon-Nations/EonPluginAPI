package org.eonnations.eonpluginapi.database;

import org.eonnations.eonpluginapi.api.economy.Vault;

public class DatabaseVault implements Vault {
    int vaultId;
    int coins;
    int iron;
    int gold;
    int diamonds;
    int emeralds;

    public DatabaseVault(int vaultId, int coins, int iron, int gold, int diamonds, int emeralds) {
        this.vaultId = vaultId;
        this.coins = coins;
        this.iron = iron;
        this.gold = gold;
        this.diamonds = diamonds;
        this.emeralds = emeralds;
    }

    // Used for Reflection
    public DatabaseVault() { }

    public static DatabaseVault invalidVault() {
        return new DatabaseVault(-1, 0, 0, 0, 0, 0);
    }

    @Override
    public boolean valid() {
        return vaultId != -1;
    }

    @Override
    public int id() {
        return vaultId;
    }

    @Override
    public int coins() {
        return coins;
    }

    @Override
    public int iron() {
        return iron;
    }

    @Override
    public int gold() {
        return gold;
    }

    @Override
    public int diamonds() {
        return diamonds;
    }

    @Override
    public int emeralds() {
        return emeralds;
    }
}
