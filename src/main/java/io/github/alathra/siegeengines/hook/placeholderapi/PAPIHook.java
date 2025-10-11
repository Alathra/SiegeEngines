package io.github.alathra.siegeengines.hook.placeholderapi;

import io.github.alathra.siegeengines.SiegeEngines;
import io.github.alathra.siegeengines.hook.AbstractHook;
import io.github.alathra.siegeengines.hook.Hook;

/**
 * A hook to interface with <a href="https://wiki.placeholderapi.com/">PlaceholderAPI</a>.
 */
public class PAPIHook extends AbstractHook {
    private PAPIExpansion PAPIExpansion;

    /**
     * Instantiates a new PlaceholderAPI hook.
     *
     * @param plugin the plugin instance
     */
    public PAPIHook(SiegeEngines plugin) {
        super(plugin);
    }

    @Override
    public void onEnable(SiegeEngines plugin) {
        if (!isHookLoaded())
            return;

        PAPIExpansion = new PAPIExpansion(super.getPlugin());
        PAPIExpansion.register();
    }

    @Override
    public void onDisable(SiegeEngines plugin) {
        if (!isHookLoaded())
            return;

        PAPIExpansion.unregister();
        PAPIExpansion = null;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.PAPI.getPluginName()) && isPluginEnabled(Hook.PAPI.getPluginName());
    }
}
