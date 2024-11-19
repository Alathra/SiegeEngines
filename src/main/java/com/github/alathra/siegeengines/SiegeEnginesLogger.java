package com.github.alathra.siegeengines;

import com.github.alathra.siegeengines.config.Config;

public class SiegeEnginesLogger {

    public static void info(String msg) {
        SiegeEngines.getInstance().getLogger().info("[" + SiegeEngines.getInstance().getName() + "] " + msg);
    }

    public static void debug(String msg) {
        if (!Config.doDebug) return;
        SiegeEngines.getInstance().getLogger().info("(DEBUG) [" + SiegeEngines.getInstance().getName() + "] " + msg);
    }

    public static void warn(String msg) {
        SiegeEngines.getInstance().getLogger().warning("[" + SiegeEngines.getInstance().getName() + "] " + msg);
    }

    public static void severe(String msg) {
        SiegeEngines.getInstance().getLogger().severe("[" + SiegeEngines.getInstance().getName() + "] " + msg);
    }
}
