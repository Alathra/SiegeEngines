package com.github.alathra.siegeengines.config;

import org.bukkit.configuration.file.FileConfiguration;

import com.github.alathra.siegeengines.SiegeEngines;


public class Config {
	
	private static FileConfiguration config;
	
	// global config version
	public static int configVersion = 1;
	
	public static void initConfigVals() {
		//init config
		config = SiegeEngines.getInstance().getConfig();
	}
	
	public static void reload() {
		// put whatever you want here to run config-wise when plugin reloads
		initConfigVals();
	}
	
	private FileConfiguration getConfig() {
		return config;
	}

}
