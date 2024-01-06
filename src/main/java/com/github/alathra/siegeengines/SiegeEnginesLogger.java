package com.github.alathra.siegeengines;

import org.bukkit.Bukkit;

public class SiegeEnginesLogger {
	
	public static void info(String msg) {
		Bukkit.getLogger().info("[" + SiegeEngines.plugin.getName() + "]" + msg);
	}
	
	public static void warn(String msg) {
		Bukkit.getLogger().warning("[" + SiegeEngines.plugin.getName() + "]" + msg);
	}
	
	public static void severe(String msg) {
		Bukkit.getLogger().severe("[" + SiegeEngines.plugin.getName() + "]" + msg);
	}
}
