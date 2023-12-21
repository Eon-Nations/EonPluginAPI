package org.eonnations.eonpluginapi.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class MenuBuilder {

    private Map<Integer, Item> inventoryMap = new HashMap<>();
    private Map<Object, Object> metadata = new HashMap<>();
    private Inventory bukkitInv;
    private Component title;

    public MenuBuilder title(String title) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        this.title = miniMessage.deserialize(title);
        return this;
    }

    public MenuBuilder withItem(int slot, Item item) {
        inventoryMap.put(slot, item);
        return this;
    }
}
