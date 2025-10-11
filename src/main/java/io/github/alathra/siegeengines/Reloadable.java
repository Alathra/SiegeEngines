package io.github.alathra.siegeengines;

/**
 * Implemented in classes that should support being reloaded IE executing the methods during runtime after startup.
 */
public interface Reloadable {
    /**
     * On plugin load.
     */
    void onLoad(SiegeEngines plugin);

    /**
     * On plugin enable.
     */
    void onEnable(SiegeEngines plugin);

    /**
     * On plugin disable.
     */
    void onDisable(SiegeEngines plugin);
}
