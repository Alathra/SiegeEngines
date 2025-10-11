package io.github.alathra.siegeengines.cooldown.listener;

import io.github.alathra.siegeengines.SiegeEngines;
import io.github.alathra.siegeengines.Reloadable;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle registration of event listeners.
 */
@SuppressWarnings("FieldCanBeLocal")
public class ListenerHandler implements Reloadable {
    private final SiegeEngines plugin;
    private final List<Listener> listeners = new ArrayList<>();

    public ListenerHandler(SiegeEngines plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(SiegeEngines plugin) {
    }

    @Override
    public void onEnable(SiegeEngines plugin) {
        listeners.clear();
        listeners.add(new CooldownListener(plugin));

        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public void onDisable(SiegeEngines plugin) {
    }
}
