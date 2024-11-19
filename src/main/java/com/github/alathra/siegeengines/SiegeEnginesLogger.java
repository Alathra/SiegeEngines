package com.github.alathra.siegeengines;

import com.github.alathra.siegeengines.config.Config;
import org.bukkit.Bukkit;

public class SiegeEnginesLogger {

    public static void info(String msg) {
        Bukkit.getLogger().info("[" + SiegeEngines.getInstance().getName() + "] " + msg);
    }

    public static void debug(String msg) {
        if (!Config.doDebug) return;
        Bukkit.getLogger().info("(DEBUG) [" + SiegeEngines.getInstance().getName() + "] " + msg);
    }

    public static void warn(String msg) {
        Bukkit.getLogger().warning("[" + SiegeEngines.getInstance().getName() + "] " + msg);
    }

    public static void severe(String msg) {
        Bukkit.getLogger().severe("[" + SiegeEngines.getInstance().getName() + "] " + msg);
    }
}
