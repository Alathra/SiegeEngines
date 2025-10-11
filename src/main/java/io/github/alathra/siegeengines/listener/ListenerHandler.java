package io.github.alathra.siegeengines.listener;

import io.github.alathra.siegeengines.SiegeEngines;
import io.github.alathra.siegeengines.Reloadable;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle registration of event listeners.
 */
public class ListenerHandler implements Reloadable {
    private final SiegeEngines plugin;
    private final List<Listener> listeners = new ArrayList<>();

    /**
     * Instantiates a the Listener handler.
     *
     * @param plugin the plugin instance
     */
    public ListenerHandler(SiegeEngines plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(SiegeEngines plugin) {
    }

    @Override
    public void onEnable(SiegeEngines plugin) {
        listeners.clear(); // Clear the list to avoid duplicate listeners when reloading the plugin
//        listeners.add(new ExampleListener());

        // Register listeners here
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public void onDisable(SiegeEngines plugin) {
    }
}
