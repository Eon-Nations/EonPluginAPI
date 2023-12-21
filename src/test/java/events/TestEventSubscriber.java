package events;

import io.vavr.Function1;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.eonnations.eonpluginapi.events.EventSubscriber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestUtility;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEventSubscriber extends TestUtility {

    private static final Function1<World, Block> blockFunc = world -> world.getBlockAt(0, 0, 0);

    @Test
    @DisplayName("Creating an event without any filters or exception handlers will be called")
    void testNoFiltersExceptions() {
        AtomicBoolean wasCalled = new AtomicBoolean(false);
        Function1<BlockBreakEvent, Boolean> handler = e -> {
            wasCalled.set(true);
            return false;
        };
        EventSubscriber.subscribe(BlockBreakEvent.class, EventPriority.NORMAL)
                .handler(handler);
        Player jim = server.addPlayer("Jim");
        BlockBreakEvent event = new BlockBreakEvent(blockFunc.apply(otherWorld), jim);
        server.getPluginManager().callEvent(event);
        assertTrue(wasCalled.get());
        server.getPluginManager().assertEventFired(BlockBreakEvent.class);
    }

    @Test
    @DisplayName("Creating an event with a filter works")
    void testWithOneFilter() {
        Player jim = server.addPlayer("Jim");
        Player bob = server.addPlayer("Bob");
        Function1<BlockBreakEvent, Boolean> filter = e -> e.getPlayer().getName().equals("Jim");
        AtomicBoolean wasCalled = new AtomicBoolean(false);
        BlockBreakEvent jimEvent = new BlockBreakEvent(blockFunc.apply(otherWorld), jim);
        BlockBreakEvent bobEvent = new BlockBreakEvent(blockFunc.apply(otherWorld), bob);
        EventSubscriber.subscribe(BlockBreakEvent.class, EventPriority.NORMAL)
                .filter(filter)
                .handler(e -> {
                    wasCalled.set(true);
                    return false;
                });
        server.getPluginManager().callEvent(jimEvent);
        assertTrue(wasCalled.get());
        wasCalled.set(false);
        server.getPluginManager().callEvent(bobEvent);
        assertFalse(wasCalled.get());
    }

    @Test
    @DisplayName("Creating an event with multiple filters works")
    void testMultipleFilters() {
        Player jim = server.addPlayer("Jim");
        jim.getInventory().addItem(new ItemStack(Material.DIRT, 32));
        Function1<BlockBreakEvent, Boolean> filter1 = e -> e.getPlayer().getName().equals("Jim");
        Function1<BlockBreakEvent, Boolean> filter2 = e -> e.getPlayer().getInventory().contains(Material.DIRT);
        AtomicBoolean wasCalled = new AtomicBoolean(false);
        BlockBreakEvent jimEvent = new BlockBreakEvent(blockFunc.apply(otherWorld), jim);
        EventSubscriber.subscribe(BlockBreakEvent.class, EventPriority.NORMAL)
                .filter(filter1)
                .filter(filter2)
                .handler(e -> {
                    wasCalled.set(true);
                    return false;
                });
        server.getPluginManager().callEvent(jimEvent);
        assertTrue(wasCalled.get());
    }

    @Test
    @DisplayName("Creating an handler that throws an exception gets the exception handler called")
    void testExceptionHandler() {
        Function1<Throwable, Boolean> exceptionHandler = t -> true;
        Player jim = server.addPlayer("Jim");
        Function1<BlockBreakEvent, Boolean> eventHandler = e -> {
            throw new RuntimeException("Nice");
        };
        EventSubscriber.subscribe(BlockBreakEvent.class, EventPriority.NORMAL)
                .exception(exceptionHandler)
                .handler(eventHandler);
        BlockBreakEvent event = new BlockBreakEvent(blockFunc.apply(otherWorld), jim);
        server.getPluginManager().callEvent(event);
        assertTrue(event.isCancelled());
    }


}
