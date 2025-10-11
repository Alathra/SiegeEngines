package io.github.alathra.siegeengines.config;

import io.github.alathra.siegeengines.SiegeEngines;
import io.github.alathra.siegeengines.Reloadable;
import io.github.milkdrinkers.crate.Config;
import io.github.milkdrinkers.crate.internal.settings.ReloadSetting;

/**
 * A class that generates/loads {@literal &} provides access to a configuration file.
 */
public class ConfigHandler implements Reloadable {
    private final SiegeEngines plugin;
    private Config cfg;

    /**
     * Instantiates a new Config handler.
     *
     * @param plugin the plugin instance
     */
    public ConfigHandler(SiegeEngines plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(SiegeEngines plugin) {
        cfg = Config.builderConfig()
            .path(plugin.getDataFolder(), "config.yml")
            .reload(ReloadSetting.MANUALLY)
            .build();
    }

    @Override
    public void onEnable(SiegeEngines plugin) {
    }

    @Override
    public void onDisable(SiegeEngines plugin) {
    }

    /**
     * Gets main config object.
     *
     * @return the config object
     */
    public Config getConfig() {
        return cfg;
    }
}
