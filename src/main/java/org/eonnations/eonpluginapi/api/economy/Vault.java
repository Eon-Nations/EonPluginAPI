package org.eonnations.eonpluginapi.api.economy;

public interface Vault {

    boolean valid();
    int id();
    int coins();
    int iron();
    int gold();
    int diamonds();
    int emeralds();

    static Vault newVault(int id, boolean valid) {
        return new Vault() {
            @Override
            public boolean valid() {
                return valid;
            }

            @Override
            public int id() {
                return id;
            }

            @Override
            public int coins() {
                return 0;
            }

            @Override
            public int iron() {
                return 0;
            }

            @Override
            public int gold() {
                return 0;
            }

            @Override
            public int diamonds() {
                return 0;
            }

            @Override
            public int emeralds() {
                return 0;
            }
        };
    }
}
