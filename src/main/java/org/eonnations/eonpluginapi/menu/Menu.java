package org.eonnations.eonpluginapi.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Menu implements InventoryHolder {
    private Map<Integer, MenuInteraction> inventoryMap = new HashMap<>();
    private Map<Object, Object> metadata = new HashMap<>();
    private Inventory bukkitInv;
    private Component title;

    Menu(InventoryType type, String rawTitle) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        Component title = miniMessage.deserialize(rawTitle);
        this.bukkitInv = Bukkit.createInventory(this, type, title);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return bukkitInv;
    }
}
