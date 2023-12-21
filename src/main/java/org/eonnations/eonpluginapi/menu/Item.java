package org.eonnations.eonpluginapi.menu;

import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.eonnations.eonpluginapi.events.EventHandler;
import org.eonnations.eonpluginapi.events.EventSubscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Item implements AutoCloseable {

    private final ItemStack item;
    private final Map<ClickType, MenuInteraction> slotHandlers;
    private final EventHandler<InventoryClickEvent> mainHandler;

    Item(ItemStack item, Map<ClickType, MenuInteraction> slotHandlers) {
        this.item = item;
        this.slotHandlers = slotHandlers;
        mainHandler = EventSubscriber.subscribe(InventoryClickEvent.class, EventPriority.NORMAL)
                .filter(e -> Objects.equals(e.getCurrentItem(), item))
                .handler(this::handleClickEvent);
    }

    public static Builder builder(ItemStack item) {
        return new Builder(item);
    }

    public static Builder builder(Material material) {
        return new Builder(material);
    }

    public static Builder builder(Material material, int amount) {
        return new Builder(material, amount);
    }

    private boolean handleClickEvent(InventoryClickEvent e) {
        MenuInteraction slotFunc = slotHandlers.getOrDefault(e.getClick(), event -> true);
        return slotFunc.apply(e);
    }

    @Override
    public void close() {
        mainHandler.close();
    }


    public static final class Builder {
        private final ItemStack item;
        private final Map<ClickType, MenuInteraction> handlers;

        public Builder(ItemStack item) {
            this.item = item;
            this.handlers = new HashMap<>();
        }

        public Builder(Material material) {
            this.item = new ItemStack(material, 1);
            this.handlers = new HashMap<>();
        }

        public Builder(Material material, int amount) {
            this.item = new ItemStack(material, amount);
            this.handlers = new HashMap<>();
        }

        public Builder handler(ClickType type, MenuInteraction func) {
            handlers.put(type, func);
            return this;
        }

        public Item finish() {
            return new Item(item, handlers);
        }
    }
}
