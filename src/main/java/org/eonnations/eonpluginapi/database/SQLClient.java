package org.eonnations.eonpluginapi.database;

import org.bukkit.entity.Player;
import org.eonnations.eonpluginapi.api.EonPlayer;
import org.eonnations.eonpluginapi.api.database.Database;
import org.eonnations.eonpluginapi.api.nations.Nation;
import org.eonnations.eonpluginapi.api.nations.Town;

import java.sql.*;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;

public class SQLClient {
    private final String url;
    private final Properties props;


    private SQLClient(Credentials credentials) throws SQLException {
        this.url = "jdbc:mysql://" + credentials.url() + "/players";
        props = new Properties();
        props.setProperty("user", credentials.user());
        props.setProperty("password", credentials.password());
    }

    public static SQLClient setupClient(Credentials credentials) {
        try {
            return new SQLClient(credentials);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SQLClient setupClient(String url, String user, String password) {
        Credentials credentials = new Credentials(url, user, password);
        return setupClient(credentials);
    }

    private <T> Result<T> sendQuery(String query, Class<T> resultClass, Consumer<Exception> handler, Object... parameters) {
        try (Connection conn = DriverManager.getConnection(url, props)) {
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i, parameters[i]);
                }
                ResultSet resultSet = statement.executeQuery(query);
                return Result.makeResultsFromSet(resultSet, resultClass);
            }
        } catch (SQLException | ReflectiveOperationException e) {
            handler.accept(e);
            return Result.empty();
        }
    }
}
