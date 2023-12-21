package menu;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.eonnations.eonpluginapi.menu.Item;
import org.eonnations.eonpluginapi.menu.Menu;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestUtility;

import static org.junit.jupiter.api.Assertions.*;

public class TestMenu extends TestUtility {


    @Test
    @DisplayName("Normal inventory interactions are not cancelled")
    void testNormalInteraction() {
        Menu menu = Menu.create(27, Component.text("Title"))
                .withItem(0, Item.builder(Material.DIRT, 32)
                        .handler(ClickType.LEFT, true, e -> true)
                        .finish())
                .complete();
        Player jim = server.addPlayer("Jim");
        ItemStack dirt = new ItemStack(Material.DIRT, 32);
        jim.getInventory().setItem(0, dirt);
        jim.openInventory(jim.getInventory());
        InventoryClickEvent event = new InventoryClickEvent(jim.getOpenInventory(), InventoryType.SlotType.CONTAINER, 0, ClickType.LEFT, InventoryAction.NOTHING);
        assertFalse(event.isCancelled());
        menu.close();
    }

    @Test
    @DisplayName("A simple menu that has items meant to be grabbed can be grabbed")
    void testNoCancelItems() {
        Menu menu = Menu.create(27, Component.text("Title"))
                .withItem(0, Item.builder(Material.DIRT, 32).finish())
                .complete();
        PlayerMock jim = server.addPlayer("Jim");
        menu.openToPlayer(jim);
        InventoryClickEvent event = jim.simulateInventoryClick(0);
        assertEquals(Material.DIRT, event.getCurrentItem().getType());
        assertFalse(event.isCancelled());
        menu.close();
    }

    @Test
    @DisplayName("A simple menu that has items not meant to be grabbed")
    void testCancelItems() {
        Menu menu = Menu.create(27, Component.text("Title"))
                .withItem(0, Item.builder(Material.DIRT, 32)
                        .handler(ClickType.LEFT, true, e -> true).finish())
                .complete();
        PlayerMock jim = server.addPlayer("Jim");
        menu.openToPlayer(jim);
        InventoryClickEvent event = jim.simulateInventoryClick(0);
        assertEquals(Material.DIRT, event.getCurrentItem().getType());
        assertTrue(event.isCancelled());
        menu.close();
    }

    @Test
    @DisplayName("If one handler is defined, setting cancelOthers will cancel the event if clicked by another type")
    void testMultipleClicks() {
        Menu menu = Menu.create(27, Component.text("Title"))
                .withItem(0, Item.builder(Material.DIRT, 32)
                        .handler(ClickType.RIGHT, true, e -> true).finish())
                .complete();
        PlayerMock jim = server.addPlayer("Jim");
        menu.openToPlayer(jim);
        InventoryClickEvent event = jim.simulateInventoryClick(0);
        assertEquals(Material.DIRT, event.getCurrentItem().getType());
        assertTrue(event.isCancelled());
        InventoryClickEvent middleClick = jim.simulateInventoryClick(jim.getOpenInventory(), ClickType.MIDDLE, 0);
        assertTrue(middleClick.isCancelled());
        menu.close();
    }
}
