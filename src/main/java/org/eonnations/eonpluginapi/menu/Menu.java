package org.eonnations.eonpluginapi.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Menu implements AutoCloseable, InventoryHolder {

    private final Map<Integer, Item> itemMap;
    private final Inventory bukkitInv;

    Menu(int size, Component title, Map<Integer, Item> itemMap) {
        this.itemMap = itemMap;
        this.bukkitInv = createBukkit(this, size, title);
        for (Map.Entry<Integer, Item> pair : itemMap.entrySet()) {
            bukkitInv.setItem(pair.getKey(), pair.getValue().item());
        }
    }

    private static Inventory createBukkit(InventoryHolder holder, int size, Component title) {
        try {
            return Bukkit.createInventory(holder, size, title);
        } catch (Exception e) {
            // Testing framework throws an error with component titles
            return Bukkit.createInventory(holder, size);
        }
    }

    public static Builder create(int size, Component title) {
        return new Builder(size, title);
    }

    public void openToPlayer(Player player) {
        player.openInventory(bukkitInv);
    }

    @Override
    public void close() {
        itemMap.values().forEach(Item::close);
        bukkitInv.close();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return bukkitInv;
    }

    public static final class Builder {
        private final Map<Integer, Item> itemMap;
        private final Component title;
        private final int size;

        public Builder(int size, Component title) {
            this.size = size;
            this.title = title;
            this.itemMap = new HashMap<>();
        }

        public Builder withItem(int slot, Item item) {
            itemMap.put(slot, item);
            return this;
        }

        public Menu complete() {
            return new Menu(size, title, itemMap);
        }
    }
}
