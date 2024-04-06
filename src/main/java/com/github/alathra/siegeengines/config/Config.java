package com.github.alathra.siegeengines.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.SiegeEnginesLogger;
import com.github.alathra.siegeengines.crafting.CraftingRecipes;
import com.github.alathra.siegeengines.data.SiegeEnginesData;
import com.github.alathra.siegeengines.projectile.EntityProjectile;
import com.github.alathra.siegeengines.projectile.ExplosiveProjectile;
import com.github.alathra.siegeengines.projectile.FireworkProjectile;
import com.github.alathra.siegeengines.projectile.PotionProjectile;
import com.github.alathra.siegeengines.projectile.ProjectileType;
import com.github.alathra.siegeengines.projectile.SiegeEngineProjectile;
import com.github.alathra.siegeengines.util.SiegeEnginesUtil;

@SuppressWarnings("deprecation")
public class Config {

	private static FileConfiguration config;

	// Universal Options - Defaults
	public static int configVersion = 1;

	public static Material controlItem = Material.CLOCK;
	public static double placementDensity = 2.5d;
	public static int controlDistance = 10;
	public static int rotateDistance = 10;
	public static int maxSiegeEnginesControlled = 5;
	public static boolean autoReload = false;
	public static boolean arrowDamageToggle = false;
	public static boolean craftingRecipes = true;
	public static boolean doDebug = false;


	public static HashSet<World> disabledWorlds = new HashSet<>();

	// Siege Engine Options - Defaults
	public static int trebuchetShotAmount = 1;
	public static float trebuchetVelocityPerFuel = 0.3f;
	public static int trebuchetMaxFuel = 3;
	public static Material trebuchetFuelItem = Material.STRING;
	public static double trebuchetHealth = 8d;
	public static boolean trebuchetCanMount = false;
	public static HashMap<ItemStack, SiegeEngineProjectile> trebuchetProjectiles = new HashMap<>();
	public static String trebuchetItemName = "&e&oTrebuchet";
	public static List<String> trebuchetItemLore;

	public static int ballistaShotAmount = 1;
	public static float ballistaVelocityPerFuel = 0.925f;
	public static double ballistaHealth = 5d;
	public static int ballistaMaxFuel = 4;
	public static Material ballistaFuelItem = Material.STRING;
	public static boolean ballistaCanMount = false;
	public static HashMap<ItemStack, SiegeEngineProjectile> ballistaProjectiles = new HashMap<>();
	public static String ballistaItemName = "&e&oBallista";
	public static List<String> ballistaItemLore;

	public static int swivelCannonShotAmount = 1;
	public static float swivelCannonVelocityPerFuel = 1.0125f;
	public static double swivelCannonHealth = 15d;
	public static int swivelCannonMaxFuel = 5;
	public static Material swivelCannonFuelItem = Material.GUNPOWDER;
	public static boolean swivelCannonCanMount = false;
	public static HashMap<ItemStack, SiegeEngineProjectile> swivelCannonProjectiles = new HashMap<>();
	public static String swivelCannonItemName = "&e&oSwivel Cannon";
	public static List<String> swivelCannonItemLore;

	public static int breachCannonShotAmount = 1;
	public static float breachCannonVelocityPerFuel = 1.075f;
	public static double breachCannonHealth = 25d;
	public static int breachCannonMaxFuel = 4;
	public static Material breachCannonFuelItem = Material.GUNPOWDER;
	public static boolean breachCannonCanMount = false;
	public static HashMap<ItemStack, SiegeEngineProjectile> breachCannonProjectiles = new HashMap<>();
	public static String breachCannonItemName = "&e&oBreach Cannon";
	public static List<String> breachCannonItemLore;

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
		placementDensity = config.getDouble("PlacementDensity");
		arrowDamageToggle = config.getBoolean("DisableArrowDamage");
		maxSiegeEnginesControlled = config.getInt("MaxSiegeEnginesControlled");
		doDebug = config.getBoolean("Debug");
		autoReload = config.getBoolean("AutoReload");
		craftingRecipes = config.getBoolean("CraftingRecipes");

		loadProjectilesConfig();
		loadSiegeEngineConfig();

	}

	private static void loadTrebuchetValues() {
		trebuchetHealth = config.getDouble("SiegeEngines.Trebuchet.Health");
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
				trebuchetProjectiles.put(projectileMap.get(projectileName).getAmmuinitionItem(),
						projectileMap.get(projectileName));
			}
		}
		trebuchetItemName = ChatColor.translateAlternateColorCodes('&',
				config.getString("SiegeEngines.Trebuchet.ItemName"));
		trebuchetItemLore = config.getStringList("SiegeEngines.Trebuchet.Lore");
		for (int i = 0; i < trebuchetItemLore.size(); i++) {
			trebuchetItemLore.set(i, ChatColor.translateAlternateColorCodes('&', trebuchetItemLore.get(i)));
		}
	}

	private static void loadBallistaValues() {
		ballistaHealth = config.getDouble("SiegeEngines.Ballista.Health");
		ballistaShotAmount = config.getInt("SiegeEngines.Ballista.ShotAmount");
		ballistaVelocityPerFuel = (float) config.getDouble("SiegeEngines.Ballista.VelocityPerFuel");
		ballistaMaxFuel = config.getInt("SiegeEngines.Ballista.MaxFuel");
		ballistaCanMount = config.getBoolean("SiegeEngines.Ballista.CanMount");
		try {
			ballistaFuelItem = Material.getMaterial(config.getString("SiegeEngines.Ballista.FuelItem"));
		} catch (Exception e) {
			ballistaFuelItem = Material.STRING;
			SiegeEnginesLogger.warn(
					"Propellant item material could not be found, defaulting to " + ballistaFuelItem.toString() + " !");
		}
		for (String projectileName : config.getStringList("SiegeEngines.Ballista.Projectiles")) {
			if (projectileMap.keySet().contains(projectileName)) {
				ballistaProjectiles.put(projectileMap.get(projectileName).getAmmuinitionItem(),
						projectileMap.get(projectileName));
			}
		}
		ballistaItemName = ChatColor.translateAlternateColorCodes('&',
				config.getString("SiegeEngines.Ballista.ItemName"));
		ballistaItemLore = config.getStringList("SiegeEngines.Ballista.Lore");
		for (int i = 0; i < ballistaItemLore.size(); i++) {
			ballistaItemLore.set(i, ChatColor.translateAlternateColorCodes('&', ballistaItemLore.get(i)));
		}
	}

	private static void loadSwivelCannonValues() {
		swivelCannonHealth = config.getDouble("SiegeEngines.SwivelCannon.Health");
		swivelCannonShotAmount = config.getInt("SiegeEngines.SwivelCannon.ShotAmount");
		swivelCannonVelocityPerFuel = (float) config.getDouble("SiegeEngines.SwivelCannon.VelocityPerFuel");
		swivelCannonMaxFuel = config.getInt("SiegeEngines.SwivelCannon.MaxFuel");
		swivelCannonCanMount = config.getBoolean("SiegeEngines.SwivelCannon.CanMount");
		try {
			swivelCannonFuelItem = Material.getMaterial(config.getString("SiegeEngines.SwivelCannon.FuelItem"));
		} catch (Exception e) {
			swivelCannonFuelItem = Material.GUNPOWDER;
			SiegeEnginesLogger.warn("Propellant item material could not be found, defaulting to "
					+ swivelCannonFuelItem.toString() + " !");
		}
		for (String projectileName : config.getStringList("SiegeEngines.SwivelCannon.Projectiles")) {
			if (projectileMap.keySet().contains(projectileName)) {
				swivelCannonProjectiles.put(projectileMap.get(projectileName).getAmmuinitionItem(),
						projectileMap.get(projectileName));
			}
		}
		swivelCannonItemName = ChatColor.translateAlternateColorCodes('&',
				config.getString("SiegeEngines.SwivelCannon.ItemName"));
		swivelCannonItemLore = config.getStringList("SiegeEngines.SwivelCannon.Lore");
		for (int i = 0; i < swivelCannonItemLore.size(); i++) {
			swivelCannonItemLore.set(i, ChatColor.translateAlternateColorCodes('&', swivelCannonItemLore.get(i)));
		}
	}

	private static void loadBreachCannonValues() {
		breachCannonHealth = config.getDouble("SiegeEngines.BreachCannon.Health");
		breachCannonShotAmount = config.getInt("SiegeEngines.BreachCannon.ShotAmount");
		breachCannonVelocityPerFuel = (float) config.getDouble("SiegeEngines.BreachCannon.VelocityPerFuel");
		breachCannonMaxFuel = config.getInt("SiegeEngines.BreachCannon.MaxFuel");
		breachCannonCanMount = config.getBoolean("SiegeEngines.BreachCannon.CanMount");
		try {
			breachCannonFuelItem = Material.getMaterial(config.getString("SiegeEngines.BreachCannon.FuelItem"));
		} catch (Exception e) {
			breachCannonFuelItem = Material.GUNPOWDER;
			SiegeEnginesLogger.warn("Propellant item material could not be found, defaulting to "
					+ breachCannonFuelItem.toString() + " !");
		}
		for (String projectileName : config.getStringList("SiegeEngines.BreachCannon.Projectiles")) {
			if (projectileMap.keySet().contains(projectileName)) {
				breachCannonProjectiles.put(projectileMap.get(projectileName).getAmmuinitionItem(),
						projectileMap.get(projectileName));
			}
		}
		breachCannonItemName = ChatColor.translateAlternateColorCodes('&',
				config.getString("SiegeEngines.BreachCannon.ItemName"));
		breachCannonItemLore = config.getStringList("SiegeEngines.BreachCannon.Lore");
		for (int i = 0; i < breachCannonItemLore.size(); i++) {
			breachCannonItemLore.set(i, ChatColor.translateAlternateColorCodes('&', breachCannonItemLore.get(i)));
		}
	}

	private static void loadSiegeEngineConfig() {
		
		loadTrebuchetValues();
		loadBallistaValues();
		loadSwivelCannonValues();
		loadBreachCannonValues();
		
		try {
			for (String worldName : config.getStringList("DisabledWorlds")) {
				World world = Bukkit.getWorld(worldName);
				if (world != null) {
					disabledWorlds.add(world);
				}
			}
			SiegeEnginesLogger.info("Disabled in worlds: "+ config.getStringList("DisabledWorlds"));
		} catch (Exception e) {
			SiegeEnginesLogger.warn("Missing Disabled Worlds-List");
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
					explosiveProjectile.explodePower = (float) config
							.getDouble("Projectiles." + projectileName + ".ExplodePower");
					explosiveProjectile.inaccuracy = (float) config
							.getDouble("Projectiles." + projectileName + ".Inaccuracy");
					explosiveProjectile.projectilesCount = config
							.getInt("Projectiles." + projectileName + ".ProjectileCount");
					explosiveProjectile.soundType = Sound
							.valueOf(config.getString("Projectiles." + projectileName + ".FireSound"));
					explosiveProjectile.velocityFactor = (float) config
							.getDouble("Projectiles." + projectileName + ".VelocityFactor");
					projectileMap.put(projectileName, explosiveProjectile);
					break;
				case ENTITY:
					EntityProjectile entityProjectile = new EntityProjectile(new ItemStack(
							Material.getMaterial(config.getString("Projectiles." + projectileName + ".AmmoItem"))));
					entityProjectile.inaccuracy = (float) config
							.getDouble("Projectiles." + projectileName + ".Inaccuracy");
					entityProjectile.projectileCount = config.getInt("Projectiles." + projectileName + ".EntityCount");
					entityProjectile.entityType = EntityType
							.valueOf(config.getString("Projectiles." + projectileName + ".EntityType"));
					entityProjectile.soundType = Sound
							.valueOf(config.getString("Projectiles." + projectileName + ".FireSound"));
					entityProjectile.velocityFactor = (float) config
							.getDouble("Projectiles." + projectileName + ".VelocityFactor");
					if (entityProjectile.entityType.equals(EntityType.ARROW)) {
						entityProjectile.arrowDamageFactor = (float) config
								.getDouble("Projectiles." + projectileName + ".ArrowDamageFactor");
					}
					projectileMap.put(projectileName, entityProjectile);
					break;
				case FIREWORK:
					FireworkProjectile fireworkProjectile = new FireworkProjectile(SiegeEnginesUtil.DEFAULT_ROCKET);
					fireworkProjectile.inaccuracy = (float) config
							.getDouble("Projectiles." + projectileName + ".Inaccuracy");
					fireworkProjectile.projectileCount = config.getInt("Projectiles." + projectileName + ".EntityCount");
					fireworkProjectile.delayTime = config.getInt("Projectiles." + projectileName + ".EntityCount");
					if (fireworkProjectile.delayTime <= 0) {
						fireworkProjectile.delayedFire = false;
					} else {
						fireworkProjectile.delayedFire = true;
					}
					fireworkProjectile.velocityFactor = (float) config
							.getDouble("Projectiles." + projectileName + ".VelocityFactor");
					projectileMap.put(projectileName, fireworkProjectile);
					break;
				case POTION:
					PotionProjectile potionProjectile = new PotionProjectile(new ItemStack(
							Material.getMaterial(config.getString("Projectiles." + projectileName + ".AmmoItem"))));
					potionProjectile.inaccuracy = (float) config
							.getDouble("Projectiles." + projectileName + ".Inaccuracy");
					potionProjectile.projectileCount = config.getInt("Projectiles." + projectileName + ".EntityCount");
					potionProjectile.delayTime = config.getInt("Projectiles." + projectileName + ".EntityCount");
					if (potionProjectile.delayTime <= 0) {
						potionProjectile.delayedFire = false;
					} else {
						potionProjectile.delayedFire = true;
					}
					potionProjectile.velocityFactor = (float) config
							.getDouble("Projectiles." + projectileName + ".VelocityFactor");
					projectileMap.put(projectileName, potionProjectile);
					break;
				default:
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void reload() {
		// put whatever you want here to run config-wise when plugin reloads
		SiegeEngines.getInstance().reloadConfig();
		SiegeEngines.getInstance().saveDefaultConfig();
		initConfigVals();
		
		// reinstantiate data class
		SiegeEnginesData.init();
		
		// load crafting recipes if enabled in config
		if (Config.craftingRecipes) {
			if (!CraftingRecipes.areLoaded()) {
				CraftingRecipes.loadCraftingRecipes();
			}
		} else {
			if (CraftingRecipes.areLoaded()) {
				CraftingRecipes.unloadCraftingRecipes();
			}
		}
	}

	public FileConfiguration getConfig() {
		return config;
	}

}
