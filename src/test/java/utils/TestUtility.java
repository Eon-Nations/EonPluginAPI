package utils;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;

public class TestUtility {
    protected ServerMock server;
    protected WorldMock otherWorld;
    protected MockPlugin plugin;

    // Bukkit is a static class, and JUnit is making multiple threads to test the code
    // In order for MockBukkit to try not to override Bukkit, the server is set to null every time
    // Bukkit.setServer(Server) throws an error when attempting, so reflection is needed
    private void setBukkitServerNull() throws NoSuchFieldException, IllegalAccessException {
        Field serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        serverField.set(serverField, null);
    }

    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        setBukkitServerNull();
        server = MockBukkit.mock();
        otherWorld = server.addSimpleWorld("other");
        plugin = MockBukkit.createMockPlugin("EonCore");
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

}
