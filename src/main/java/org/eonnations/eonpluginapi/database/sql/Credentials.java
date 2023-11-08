package org.eonnations.eonpluginapi.database.sql;

import org.eonnations.eonpluginapi.EonPlugin;

import java.util.Optional;

public record Credentials(String url, String port, String user, String password, String database) {

    public static Credentials credentials(EonPlugin plugin) {
        String url = Optional.ofNullable(plugin.getConfig().getString("mysql-url"))
                .orElse("localhost");
        String port = Optional.ofNullable(plugin.getConfig().getString("mysql-port"))
                .orElse("3306");
        String user = Optional.ofNullable(plugin.getConfig().getString("mysql-user"))
                .orElse("root");
        String password = Optional.ofNullable(plugin.getConfig().getString("mysql-password"))
                .orElse("root-password");
        String database = Optional.ofNullable(plugin.getConfig().getString("mysql-database"))
                .orElse("server");
        return new Credentials(url, port, user, password, database);
    }
}
