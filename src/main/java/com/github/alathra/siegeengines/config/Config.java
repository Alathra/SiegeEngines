package com.github.alathra.siegeengines.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.SiegeEnginesLogger;


public class Config {
	
	private static FileConfiguration config;
	
	// DEFAULTS
	public static int configVersion = 1;
	
	public static Material controlItem  = Material.COMPASS;
	
	public static void initConfigVals() {
		// init config
		config = SiegeEngines.getInstance().getConfig();
		
		// get config values
		try {
			controlItem = Material.getMaterial(config.getString("ControlItem"));
		} catch (Exception e) {
			SiegeEnginesLogger.warn("Control item material could not be found, defaulting to compass!");
			controlItem = Material.COMPASS;
		}
		
	}
	
	public static void reload() {
		// put whatever you want here to run config-wise when plugin reloads
		initConfigVals();
	}
	
	private FileConfiguration getConfig() {
		return config;
	}

}
