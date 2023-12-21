package menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.eonnations.eonpluginapi.menu.Item;
import org.eonnations.eonpluginapi.menu.MenuInteraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestUtility;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestItem extends TestUtility {

    @Test
    @DisplayName("An item that cancels event should cancel the Click event")
    void testCancelClickEvent() {
        ItemStack bukkitItem = new ItemStack(Material.DIRT, 32);
        Player jim = server.addPlayer("Jim");
        jim.getInventory().setItem(0, bukkitItem);
        Item item = Item.builder(bukkitItem)
                .handler(ClickType.LEFT, true, e -> true)
                .finish();
        jim.openInventory(jim.getInventory());
        InventoryClickEvent event = new InventoryClickEvent(jim.getOpenInventory(), InventoryType.SlotType.CONTAINER, 0, ClickType.LEFT, InventoryAction.NOTHING);
        server.getPluginManager().callEvent(event);
        assertTrue(event.isCancelled());
        item.close();
    }

    @Test
    @DisplayName("An item that does not cancel the Click event should not cancel the event")
    void testNoCancelClickEvent() {
        ItemStack bukkitItem = new ItemStack(Material.DIRT, 32);
        Player jim = server.addPlayer("Jim");
        jim.getInventory().setItem(0, bukkitItem);
        Item item = Item.builder(bukkitItem)
                .handler(ClickType.LEFT, true, e -> false)
                .finish();
        jim.openInventory(jim.getInventory());
        InventoryClickEvent event = new InventoryClickEvent(jim.getOpenInventory(), InventoryType.SlotType.CONTAINER, 0, ClickType.LEFT, InventoryAction.NOTHING);
        server.getPluginManager().callEvent(event);
        assertFalse(event.isCancelled());
        item.close();
    }

    @Test
    @DisplayName("Different clicks will call different functions")
    void testDifferentClicks() {
        ItemStack bukkitItem = new ItemStack(Material.DIRT, 32);
        Player jim = server.addPlayer("Jim");
        jim.getInventory().setItem(0, bukkitItem);
        AtomicBoolean wasFirstCalled = new AtomicBoolean(false);
        MenuInteraction first = e -> {
            wasFirstCalled.set(true);
            return true;
        };
        AtomicBoolean wasSecondCalled = new AtomicBoolean(false);
        MenuInteraction second = e -> {
            wasSecondCalled.set(true);
            return true;
        };
        Item item = Item.builder(bukkitItem)
                .handler(ClickType.LEFT, true, first)
                .handler(ClickType.MIDDLE, true, second)
                .finish();
        jim.openInventory(jim.getInventory());
        InventoryClickEvent leftClick = new InventoryClickEvent(jim.getOpenInventory(), InventoryType.SlotType.CONTAINER, 0, ClickType.LEFT, InventoryAction.NOTHING);
        server.getPluginManager().callEvent(leftClick);
        assertTrue(wasFirstCalled.get());
        assertTrue(leftClick.isCancelled());
        InventoryClickEvent middleClick = new InventoryClickEvent(jim.getOpenInventory(), InventoryType.SlotType.CONTAINER, 0, ClickType.MIDDLE, InventoryAction.NOTHING);
        server.getPluginManager().callEvent(middleClick);
        assertTrue(wasSecondCalled.get());
        assertTrue(middleClick.isCancelled());
        item.close();
    }
}
