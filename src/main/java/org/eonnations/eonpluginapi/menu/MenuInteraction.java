package org.eonnations.eonpluginapi.menu;

import io.vavr.Function1;
import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface MenuInteraction extends Function1<InventoryClickEvent, Boolean> {

}
