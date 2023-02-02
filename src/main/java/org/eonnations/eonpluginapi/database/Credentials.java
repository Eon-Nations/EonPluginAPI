package org.eonnations.eonpluginapi.database;

import org.eonnations.eonpluginapi.EonPlugin;

import java.util.Optional;

public record Credentials(String url, String user, String password) {

    public static Credentials credentials(EonPlugin plugin) {
        String url = Optional.ofNullable(plugin.getConfig().getString("mysql-url"))
                .orElse("localhost:3306");
        String user = Optional.ofNullable(plugin.getConfig().getString("mysql-user"))
                .orElse("root");
        String password = Optional.ofNullable(plugin.getConfig().getString("mysql-password"))
                .orElse("root-password");
        return new Credentials(url, user, password);
    }
}
