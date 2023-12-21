package events;

import io.vavr.Function1;
import io.vavr.collection.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.eonnations.eonpluginapi.events.EventHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestUtility;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEventHandler extends TestUtility {

    private static final Function1<World, Block> blockFunc = world -> world.getBlockAt(0, 0, 0);

    @Test
    @DisplayName("When an exception is thrown when filtering, the handler is called and it gracefully exits")
    void testFilterException() {
        AtomicBoolean wasCalled = new AtomicBoolean(false);
        Function1<BlockBreakEvent, Boolean> exceptionFilter = e -> {
            wasCalled.set(true);
            throw new RuntimeException("Nice");
        };
        Player jim = server.addPlayer("Jim");
        BlockBreakEvent event = new BlockBreakEvent(blockFunc.apply(otherWorld), jim);
        new EventHandler<>(plugin, BlockBreakEvent.class, EventPriority.NORMAL, List.of(exceptionFilter), t -> true, e -> false);
        server.getPluginManager().callEvent(event);
        assertTrue(wasCalled.get());
    }

    @Test
    @DisplayName("When an exception is thrown in the handler, the exception function is called and it gracefully exits")
    void testHandlerException() {
        Player jim = server.addPlayer("Jim");
        BlockBreakEvent event = new BlockBreakEvent(blockFunc.apply(otherWorld), jim);
        Function1<BlockBreakEvent, Boolean> handler = e -> {
            throw new RuntimeException("Will it catch it?");
        };
        AtomicBoolean isExceptionCalled = new AtomicBoolean(false);
        Function1<Throwable, Boolean> exception = e -> {
            isExceptionCalled.set(true);
            return false;
        };
        new EventHandler<>(plugin, BlockBreakEvent.class, EventPriority.NORMAL, List.of(), exception, handler);
        server.getPluginManager().callEvent(event);
        assertTrue(isExceptionCalled.get());
    }


    @Test
    @DisplayName("If a filter is false, the handler isn't called")
    void testFilter() {
        Player jim = server.addPlayer("Jim");
        BlockBreakEvent event = new BlockBreakEvent(blockFunc.apply(otherWorld), jim);
        Function1<BlockBreakEvent, Boolean> falseFilter = e -> e.getPlayer().getName().equals("Another name");
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        new EventHandler<>(plugin, BlockBreakEvent.class, EventPriority.NORMAL, List.of(falseFilter), e -> false, e -> {
            handlerCalled.set(true);
            return false;
        });
        server.getPluginManager().callEvent(event);
        assertFalse(handlerCalled.get());
    }

    @Test
    @DisplayName("If there are multiple filters and they are all true, then the event will be called")
    void testMultipleFilters() {
        Player jim = server.addPlayer("Jim");
        BlockBreakEvent event = new BlockBreakEvent(blockFunc.apply(otherWorld), jim);
        Function1<BlockBreakEvent, Boolean> filter1 = e -> e.getPlayer().getName().equals("Jim");
        Function1<BlockBreakEvent, Boolean> filter2 = e -> !e.getPlayer().getInventory().contains(Material.DIRT);
        AtomicBoolean wasCalled = new AtomicBoolean(false);
        new EventHandler<>(plugin, BlockBreakEvent.class, EventPriority.NORMAL, List.of(filter1, filter2), t -> false, e -> {
            wasCalled.set(true);
            return false;
        });
        server.getPluginManager().callEvent(event);
        assertFalse(event.isCancelled());
        assertTrue(wasCalled.get());
    }

    @Test
    @DisplayName("If the event is cancelled in the handler, the event is cancelled in Bukkit")
    void testCancelWithBukkit() {
        Player jim = server.addPlayer("Jim");
        BlockBreakEvent event = new BlockBreakEvent(blockFunc.apply(otherWorld), jim);
        new EventHandler<>(plugin, BlockBreakEvent.class, EventPriority.NORMAL, List.of(), t -> false, e -> true);
        server.getPluginManager().callEvent(event);
        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("Without any filters, the handler is called")
    void testHandler() {
        Player jim = server.addPlayer("Jim");
        BlockBreakEvent event = new BlockBreakEvent(blockFunc.apply(otherWorld), jim);
        new EventHandler<>(plugin, BlockBreakEvent.class, EventPriority.NORMAL, List.of(), t -> false, e -> {
            Player p = e.getPlayer();
            p.getInventory().addItem(new ItemStack(Material.DIRT, 32));
            return false;
        });
        server.getPluginManager().callEvent(event);
        assertTrue(jim.getInventory().contains(Material.DIRT));
    }

    @Test
    @DisplayName("AutoClosable method will unregister the listener")
    void testAutoClose() {
        Player jim = server.addPlayer("Jim");
        PlayerJoinEvent joinEvent = new PlayerJoinEvent(jim, Component.text("Nice"));
        AtomicBoolean wasCalled = new AtomicBoolean(false);
        EventHandler<PlayerJoinEvent> handler = new EventHandler<>(plugin, PlayerJoinEvent.class, EventPriority.NORMAL, List.of(), e -> false, e -> {
            wasCalled.set(true);
            return false;
        });
        handler.close();
        server.getPluginManager().callEvent(joinEvent);
        assertFalse(wasCalled.get());
    }

    @Test
    @DisplayName("Unregistering a EventHandler multiple times doesn't throw an error")
    void testMultipleCloses() {
        Player jim = server.addPlayer("Jim");
        PlayerJoinEvent joinEvent = new PlayerJoinEvent(jim, Component.text("Nice"));
        AtomicBoolean wasCalled = new AtomicBoolean(false);
        EventHandler<PlayerJoinEvent> handler = new EventHandler<>(plugin, PlayerJoinEvent.class, EventPriority.NORMAL, List.of(), e -> false, e -> {
            wasCalled.set(true);
            return false;
        });
        handler.close();
        handler.close();
        handler.close();
        server.getPluginManager().callEvent(joinEvent);
        assertFalse(wasCalled.get());
    }
}
