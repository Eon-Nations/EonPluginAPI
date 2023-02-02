package org.eonnations.eonpluginapi.api.economy;

import org.eonnations.eonpluginapi.EonPlugin;
import org.eonnations.eonpluginapi.api.Status;
import org.eonnations.eonpluginapi.database.Credentials;
import org.eonnations.eonpluginapi.database.SQLClient;

public class Economy {
    private Economy() { }

    public static Deposit provideDeposit(EonPlugin plugin) {
        Deposit deposit = (uuid, amount) -> {
            Credentials credentials = Credentials.credentials(plugin);
            SQLClient client = SQLClient.setupClient(credentials);
            return Status.SUCCESS;
        };
        plugin.provideService(Deposit.class, deposit);
        return deposit;
    }
}
