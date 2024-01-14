package com.github.alathra.siegeengines.config;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.SiegeEnginesLogger;
import com.github.alathra.siegeengines.SiegeEnginesUtil;
import com.github.alathra.siegeengines.projectile.EntityProjectile;
import com.github.alathra.siegeengines.projectile.ExplosiveProjectile;
import com.github.alathra.siegeengines.projectile.FireworkProjectile;
import com.github.alathra.siegeengines.projectile.ProjectileType;
import com.github.alathra.siegeengines.projectile.SiegeEngineProjectile;

public class Config {

	private static FileConfiguration config;

	// Universal Options - Defaults
	public static int configVersion = 1;

	public static Material controlItem = Material.CLOCK;
	public static int controlDistance = 64;
	public static int rotateDistance = 32;
	public static int maxSiegeEnginesControlled = 5;
	public static boolean autoReload = false;

	// Siege Engine Options - Defaults
	public static int trebuchetShotAmount = 1;
	public static float trebuchetVelocityPerFuel = 0.3f;
	public static int trebuchetMaxFuel = 5;
	public static Material trebuchetFuelItem = Material.STRING;

	public static int siegeCannonShotAmount = 1;
	public static float siegeCannonVelocityPerFuel = 1.0125f;
	public static int siegeCannonMaxFuel = 5;
	public static Material siegeCannonFuelItem = Material.STRING;

	public static int navalCannonShotAmount = 1;
	public static float navalCannonVelocityPerFuel = 1.0125f;
	public static int navalCannonMaxFuel = 5;
	public static Material navalCannonFuelItem = Material.STRING;
	
	public static HashMap<ItemStack, SiegeEngineProjectile> trebuchetProjectiles = new HashMap<>();
	public static HashMap<ItemStack, SiegeEngineProjectile> siegeCannonProjectiles = new HashMap<>();
	public static HashMap<ItemStack, SiegeEngineProjectile> navalCannonProjectiles = new HashMap<>();

	// Projectiles

	public static LinkedHashMap<String, SiegeEngineProjectile> projectileMap = new LinkedHashMap<>();

	public static void initConfigVals() {
		// init config
		config = SiegeEngines.getInstance().getConfig();

		// GENERAL

		try {
			controlItem = Material.getMaterial(config.getString("ControlItem"));
		} catch (Exception e) {
			controlItem = Material.CLOCK;
			SiegeEnginesLogger
					.warn("Control item material could not be found, defaulting to " + controlItem.toString() + " !");
		}

		controlDistance = config.getInt("ControlDistance");
		rotateDistance = config.getInt("RotateDistance");
		maxSiegeEnginesControlled = config.getInt("MaxSiegeEnginesControlled");
		autoReload = config.getBoolean("AutoReload");
		
		loadProjectilesConfig();
		loadSiegeEngineConfig();
		
		
	}

	private static void loadSiegeEngineConfig() {
		trebuchetShotAmount = config.getInt("SiegeEngines.Trebuchet.ShotAmount");
		trebuchetVelocityPerFuel = (float) config.getDouble("SiegeEngines.Trebuchet.VelocityPerFuel");
		trebuchetMaxFuel = config.getInt("SiegeEngines.Trebuchet.MaxFuel");
		try {
			trebuchetFuelItem = Material.getMaterial(config.getString("SiegeEngines.Trebuchet.FuelItem"));
		} catch (Exception e) {
			trebuchetFuelItem = Material.STRING;
			SiegeEnginesLogger.warn("Propellant item material could not be found, defaulting to "
					+ trebuchetFuelItem.toString() + " !");
		}
		for (String projectileName : config.getStringList("SiegeEngines.Trebuchet.Projectiles")) {
			if (projectileMap.keySet().contains(projectileName)) {
				trebuchetProjectiles.put(projectileMap.get(projectileName).getAmmuinitionItem(), projectileMap.get(projectileName));
			}
		}

		siegeCannonShotAmount = config.getInt("SiegeEngines.SiegeCannon.ShotAmount");
		siegeCannonVelocityPerFuel = (float) config.getDouble("SiegeEngines.SiegeCannon.VelocityPerFuel");
		siegeCannonMaxFuel = config.getInt("SiegeEngines.SiegeCannon.MaxFuel");
		try {
			siegeCannonFuelItem = Material.getMaterial(config.getString("SiegeEngines.SiegeCannon.FuelItem"));
		} catch (Exception e) {
			siegeCannonFuelItem = Material.GUNPOWDER;
			SiegeEnginesLogger.warn("Propellant item material could not be found, defaulting to "
					+ siegeCannonFuelItem.toString() + " !");
		}
		for (String projectileName : config.getStringList("SiegeEngines.SiegeCannon.Projectiles")) {
			if (projectileMap.keySet().contains(projectileName)) {
				siegeCannonProjectiles.put(projectileMap.get(projectileName).getAmmuinitionItem(), projectileMap.get(projectileName));
			}
		}

		navalCannonShotAmount = config.getInt("SiegeEngines.NavalCannon.ShotAmount");
		navalCannonVelocityPerFuel = (float) config.getDouble("SiegeEngines.NavalCannon.VelocityPerFuel");
		navalCannonMaxFuel = config.getInt("SiegeEngines.NavalCannon.MaxFuel");
		try {
			navalCannonFuelItem = Material.getMaterial(config.getString("SiegeEngines.NavalCannon.FuelItem"));
		} catch (Exception e) {
			navalCannonFuelItem = Material.GUNPOWDER;
			SiegeEnginesLogger.warn("Propellant item material could not be found, defaulting to "
					+ navalCannonFuelItem.toString() + " !");
		}
		for (String projectileName : config.getStringList("SiegeEngines.NavalCannon.Projectiles")) {
			if (projectileMap.keySet().contains(projectileName)) {
				navalCannonProjectiles.put(projectileMap.get(projectileName).getAmmuinitionItem(), projectileMap.get(projectileName));
			}
		}
	}

	private static void loadProjectilesConfig() {
		projectileMap.clear();
		
		for (String projectileName : config.getConfigurationSection("Projectiles").getKeys(false)) {
			
			ProjectileType projectileType = null;
			try {
				projectileType = ProjectileType
						.valueOf(config.getString("Projectiles." + projectileName + ".ProjectileType"));
			} catch (Exception e) {
				SiegeEnginesLogger
						.warn("Could not load projectile - " + projectileName + " likely due to a config error!");
				continue;
			}
			
			try {
				switch (projectileType) {
				case EXPLOSIVE:
					ExplosiveProjectile explosiveProjectile = new ExplosiveProjectile(new ItemStack(
							Material.getMaterial(config.getString("Projectiles." + projectileName + ".AmmoItem"))));
					explosiveProjectile.explodePower = (float) config.getDouble("Projectiles." + projectileName + ".ExplodePower");
					explosiveProjectile.inaccuracy = (float) config.getDouble("Projectiles." + projectileName + ".Inaccuracy");
					explosiveProjectile.projectilesCount = config.getInt("Projectiles." + projectileName + ".ProjectileCount");
					projectileMap.put(projectileName, explosiveProjectile);
					break;
				case ENTITY:
					EntityProjectile entityProjectile = new EntityProjectile(new ItemStack(
							Material.getMaterial(config.getString("Projectiles." + projectileName + ".AmmoItem"))));
					entityProjectile.inaccuracy = (float) config.getDouble("Projectiles." + projectileName + ".Inaccuracy");
					entityProjectile.entityCount = config.getInt("Projectiles." + projectileName + ".EntityCount");
					entityProjectile.entityType = EntityType.valueOf(config.getString("Projectiles." + projectileName + ".EntityShotType"));
					projectileMap.put(projectileName, entityProjectile);
					break;
				case FIREWORK:
					FireworkProjectile fireworkProjectile = new FireworkProjectile(SiegeEnginesUtil.DEFAULT_ROCKET);
					fireworkProjectile.inaccuracy = (float) config.getDouble("Projectiles." + projectileName + ".Inaccuracy");
					fireworkProjectile.entityCount = config.getInt("Projectiles." + projectileName + ".EntityCount");
					fireworkProjectile.delayTime = config.getInt("Projectiles." + projectileName + ".EntityCount");
					if (fireworkProjectile.delayTime <= 0) {
					fireworkProjectile.delayedFire = false;
					} else {
						fireworkProjectile.delayedFire = true;
					}
					fireworkProjectile.velocityFactor = (float) config.getDouble("Projectiles." + projectileName + ".VelocityFactor");
					projectileMap.put(projectileName, fireworkProjectile);
					break;
				default:
					continue;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void reload() {
		// put whatever you want here to run config-wise when plugin reloads
		initConfigVals();
	}

	public FileConfiguration getConfig() {
		return config;
	}

}
