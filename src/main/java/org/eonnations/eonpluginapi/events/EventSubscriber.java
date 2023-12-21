package org.eonnations.eonpluginapi.events;

import io.vavr.Function1;
import io.vavr.collection.List;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

/*
Inspired by lucko's helper
 */
public class EventSubscriber<T extends Event & Cancellable> {
    private List<Function1<T, Boolean>> filters;
    private Function1<Throwable, Boolean> exceptionHandler = t -> false;
    private final Class<T> eventClass;
    private final EventPriority priority;
    private final JavaPlugin plugin;

    EventSubscriber(Class<T> eventClass, EventPriority priority, JavaPlugin plugin) {
        this.filters = List.of();
        this.eventClass = eventClass;
        this.priority = priority;
        this.plugin = plugin;
    }

    public static <V extends Event & Cancellable> EventSubscriber<V> subscribe(Class<V> eventClass, EventPriority priority) {
        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("EonCore");
        return new EventSubscriber<>(eventClass, priority, plugin);
    }

    public EventSubscriber<T> filter(Function1<T, Boolean> filter) {
        this.filters = filters.append(filter);
        return this;
    }

    public EventSubscriber<T> exception(Function1<Throwable, Boolean> handle) {
        this.exceptionHandler = handle;
        return this;
    }

    public EventHandler<T> handler(Function1<T, Boolean> handler) {
        return new EventHandler<>(plugin, eventClass, priority, filters, exceptionHandler, handler);
    }
}
