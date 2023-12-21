package org.eonnations.eonpluginapi.menu;

import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.eonnations.eonpluginapi.events.EventHandler;
import org.eonnations.eonpluginapi.events.EventSubscriber;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Item implements AutoCloseable {

    private final ItemStack item;
    private final Map<ClickType, MenuInteraction> slotHandlers;
    private final EventHandler<InventoryClickEvent> mainHandler;

    Item(ItemStack item, Map<ClickType, MenuInteraction> slotHandlers) {
        this.item = item;
        this.slotHandlers = slotHandlers;
        this.mainHandler = EventSubscriber.subscribe(InventoryClickEvent.class, EventPriority.NORMAL)
                .filter(e -> slotHandlers.containsKey(e.getClick()))
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

    public ItemStack item() {
        return item;
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
            this.handlers = new EnumMap<>(ClickType.class);
        }

        public Builder(Material material) {
            this.item = new ItemStack(material, 1);
            this.handlers = new EnumMap<>(ClickType.class);
        }

        public Builder(Material material, int amount) {
            this.item = new ItemStack(material, amount);
            this.handlers = new EnumMap<>(ClickType.class);
        }

        public Builder handler(ClickType type, boolean cancelOtherClicks, MenuInteraction func) {
            handlers.put(type, func);
            if (cancelOtherClicks) {
                ClickType[] others = ClickType.values();
                for (ClickType other : others) {
                    handlers.putIfAbsent(other, e -> true);
                }
            }
            return this;
        }

        public Item finish() {
            return new Item(item, handlers);
        }
    }
}
