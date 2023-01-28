package org.eonnations.eonpluginapi;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class EonPlugin extends JavaPlugin {

    public abstract void load();
    public abstract void setup();
    public abstract void cleanup();

    @Override
    public void onLoad() {
        load();
    }

    @Override
    public void onEnable() {
        setup();
    }

    @Override
    public void onDisable() {
        cleanup();
    }

    public <T> T getService(Class<T> service) {
        RegisteredServiceProvider<T> provider = getServer().getServicesManager().getRegistration(service);
        return provider.getProvider();
    }

    public <T> void provideService(Class<T> serviceClass, T service) {
        provideService(serviceClass, service, ServicePriority.Normal);
    }

    public <T> void provideService(Class<T> serviceClass, T service, ServicePriority priority) {
        ServicesManager manager = getServer().getServicesManager();
        manager.register(serviceClass, service, this, priority);
    }

}
