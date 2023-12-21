package org.eonnations.eonpluginapi.menu;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

@FunctionalInterface
public interface MenuInteraction extends Consumer<InventoryClickEvent> {

}
