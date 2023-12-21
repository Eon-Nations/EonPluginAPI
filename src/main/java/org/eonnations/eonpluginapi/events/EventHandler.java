package org.eonnations.eonpluginapi.events;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventHandler<T extends Event> implements EventExecutor, Listener, AutoCloseable {
    private final Class<T> eventClass;
    private final EventPriority priority;
    private final Function1<Throwable, Boolean> exceptionHandler;
    private final List<Function1<T, Boolean>> filters;
    private final Function1<T, Boolean> handler;
    private final AtomicBoolean isActive = new AtomicBoolean(true);

    public EventHandler(JavaPlugin plugin, Class<T> eventClass, EventPriority priority, List<Function1<T, Boolean>> filters, Function1<Throwable, Boolean> exceptionHandler, Function1<T, Boolean> handler) {
        this.eventClass = eventClass;
        this.priority = priority;
        this.filters = filters;
        this.exceptionHandler = exceptionHandler;
        this.handler = handler;
        registerEventWithBukkit(plugin);
    }

    private void registerEventWithBukkit(JavaPlugin plugin) {
        PluginManager manager = plugin.getServer().getPluginManager();
        manager.registerEvent(eventClass, this, priority, this, plugin, false);
    }

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        if (!event.getClass().equals(eventClass)) {
            return;
        }

        T eventInstance = eventClass.cast(event);
        boolean passAllChecks = Try.of(() -> filters.map(f -> f.apply(eventInstance))
                .forAll(b -> b))
                .getOrElse(false);
        if (!passAllChecks) return;

        boolean isCancelled = Try.of(() -> handler.apply(eventInstance))
                .recover(exceptionHandler)
                .get();
        if (isCancelled) {
            cancelEvent(eventInstance).peekLeft(Exception::printStackTrace);
        }
    }

    private Either<Exception, Boolean> cancelEvent(T event) {
        try {
            Method method = event.getClass().getMethod("setCancelled", boolean.class);
            method.invoke(event, true);
            return Either.right(true);
        } catch (Exception e) {
            return Either.left(e);
        }
    }


    private void unregister() {
        try {
            Method getHanderList = eventClass.getMethod("getHandlerList");
            HandlerList handlerList = (HandlerList) getHanderList.invoke(null);
            handlerList.unregister(this);
        } catch (Exception e) {
            // No action needed
        }
    }

    @Override
    public void close() {
        if (!isActive.get()) return;
        isActive.set(false);
        unregister();
    }
}
