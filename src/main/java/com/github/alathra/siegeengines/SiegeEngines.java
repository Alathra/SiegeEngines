package com.github.alathra.siegeengines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.github.alathra.siegeengines.command.CommandHandler;
import com.github.alathra.siegeengines.config.Config;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.metadata.MetadataValueAdapter;

import com.github.alathra.siegeengines.listeners.ClickHandler;
import com.github.alathra.siegeengines.listeners.RotationHandler;
import com.github.alathra.siegeengines.listeners.PlayerHandler;

public class SiegeEngines extends JavaPlugin {

	private CommandHandler commandHandler;

	public static SiegeEngines instance;
	public static MetadataValueAdapter metadata;
	public static Random random = new Random();

	// model id, defined seige engine types
	public static HashMap<Integer, SiegeEngine> definedSiegeEngines = new HashMap<Integer, SiegeEngine>();
	// SiegeEngine entity, SiegeEngine object
	public static HashMap<UUID, SiegeEngine> activeSiegeEngines = new HashMap<UUID, SiegeEngine>();
	// Player UUID, SiegeEngine entity
	public static HashMap<UUID, List<Entity>> siegeEngineEntitiesPerPlayer = new HashMap<UUID, List<Entity>>();

	public static SiegeEngines getInstance() {
		return instance;
	}

	@Override
	public void onLoad() {
		commandHandler = new CommandHandler(this);
		commandHandler.onLoad();
		instance = this;
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		Config.reload();
		activeSiegeEngines.clear();
		siegeEngineEntitiesPerPlayer.clear();
		definedSiegeEngines.clear();
		getServer().getPluginManager().registerEvents(new RotationHandler(), this);
		getServer().getPluginManager().registerEvents(new ClickHandler(), this);
		getServer().getPluginManager().registerEvents(new PlayerHandler(), this);
		addDefaults();
		for (SiegeEngine i : definedSiegeEngines.values()) {
			System.out.println("§eEnabled Weapon : " + i.name);
			System.out.println("§eWeapon Propellant/\"Fuel\" ItemStacks : " + i.fuelItem);
			for (ItemStack proj : i.projectiles.keySet()) {
				System.out.println("§eWeapon Projectile ItemStacks : " + proj);
			}
		}
		commandHandler.onEnable();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onDisable() {
		commandHandler.onDisable();
		for (SiegeEngine siegeEngine : activeSiegeEngines.values()) {
			if (siegeEngine.entity != null) {
				ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
				ItemMeta meta = item.getItemMeta();
				siegeEngine.ammoHolder = new SiegeEngineAmmoHolder();
				meta.setCustomModelData(siegeEngine.readyModelNumber);
				switch(siegeEngine.type) {
					case TREBUCHET:
						meta.setDisplayName(Config.trebuchetItemName);
						meta.setLore(Config.trebuchetItemLore);
						break;
					case BALLISTA:
						meta.setDisplayName(Config.ballistaItemName);
						meta.setLore(Config.ballistaItemLore);
						break;
					case SWIVEL_CANNON:
						meta.setDisplayName(Config.swivelCannonItemName);
						meta.setLore(Config.swivelCannonItemLore);
						break;
					case BREACH_CANNON:
						meta.setDisplayName(Config.breachCannonItemName);
						meta.setLore(Config.ballistaItemLore);
						break;
				}
				item.setItemMeta(meta);

				((LivingEntity) siegeEngine.entity).getEquipment().setHelmet(item);
			}
		}
	}

	public static void addDefaults() {

		// Trebuchet
		SiegeEngine trebuchet = new SiegeEngine("Trebuchet", Config.trebuchetProjectiles,
				new ItemStack(Material.STRING), 122);
		// config options
		trebuchet.type = SiegeEngineType.TREBUCHET;
		trebuchet.itemName = Config.trebuchetItemName;
		trebuchet.itemLore = Config.trebuchetItemLore;
		trebuchet.shotAmount = Config.trebuchetShotAmount;
		trebuchet.velocityPerFuel = Config.trebuchetVelocityPerFuel;
		trebuchet.maxFuel = Config.trebuchetMaxFuel;
		trebuchet.fuelItem = new ItemStack(Config.trebuchetFuelItem);
		trebuchet.projectiles = Config.trebuchetProjectiles;
		trebuchet.xOffset = 3;
		trebuchet.yOffset = 3;
		trebuchet.placementOffsetY = 0.0;
		trebuchet.rotateStandHead = false;
		trebuchet.rotateSideways = true;
		trebuchet.readyModelNumber = 122;
		trebuchet.modelNumberToFireAt = 135;
		trebuchet.millisecondsBetweenFiringStages = 2;
		trebuchet.millisecondsBetweenReloadingStages = 10;
		trebuchet.firingModelNumbers = new ArrayList<>(
				Arrays.asList(123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139));
		trebuchet.cycleThroughModelsWhileFiring = true;
		definedSiegeEngines.put(trebuchet.readyModelNumber, trebuchet);

		// Ballista
		SiegeEngine ballista = new SiegeEngine("Ballista", Config.ballistaProjectiles, new ItemStack(Material.STRING),
				145);
		// config options
		ballista.type = SiegeEngineType.BALLISTA;
		ballista.itemName = Config.ballistaItemName;
		ballista.itemLore = Config.ballistaItemLore;
		ballista.shotAmount = Config.ballistaShotAmount;
		ballista.velocityPerFuel = Config.ballistaVelocityPerFuel;
		ballista.maxFuel = Config.ballistaMaxFuel;
		ballista.fuelItem = new ItemStack(Config.ballistaFuelItem);
		ballista.projectiles = Config.ballistaProjectiles;
		ballista.xOffset = 1;
		ballista.yOffset = 1;
		ballista.placementOffsetY = -0.75;
		ballista.rotateStandHead = true;
		ballista.rotateSideways = true;
		ballista.setModelNumberWhenFullyLoaded = true;
		ballista.readyModelNumber = 145;
		ballista.modelNumberToFireAt = 145;
		ballista.preFireModelNumber = 143;
		ballista.firingModelNumbers = new ArrayList<>(Arrays.asList(143, 144, 145));
		ballista.cycleThroughModelsWhileFiring = true;
		definedSiegeEngines.put(ballista.readyModelNumber, ballista);

		// Siege Cannon
		SiegeEngine swivelCannon = new SiegeEngine("Swivel Cannon", Config.swivelCannonProjectiles,
				new ItemStack(Material.GUNPOWDER), 141);
		// config options
		swivelCannon.type = SiegeEngineType.SWIVEL_CANNON;
		swivelCannon.itemName = Config.swivelCannonItemName;
		swivelCannon.itemLore = Config.swivelCannonItemLore;
		swivelCannon.shotAmount = Config.swivelCannonShotAmount;
		swivelCannon.velocityPerFuel = Config.swivelCannonVelocityPerFuel;
		swivelCannon.maxFuel = Config.swivelCannonMaxFuel;
		swivelCannon.fuelItem = new ItemStack(Config.swivelCannonFuelItem);
		swivelCannon.projectiles = Config.swivelCannonProjectiles;
		swivelCannon.placementOffsetY = -1;
		swivelCannon.readyModelNumber = 141;
		swivelCannon.modelNumberToFireAt = 141;
		swivelCannon.firingModelNumbers = new ArrayList<Integer>();
		swivelCannon.rotateStandHead = true;
		swivelCannon.rotateSideways = true;
		definedSiegeEngines.put(swivelCannon.readyModelNumber, swivelCannon);

		// Naval Cannon
		SiegeEngine beachCannon = new SiegeEngine("Breach Cannon", Config.breachCannonProjectiles,
				new ItemStack(Material.GUNPOWDER), 142);
		// config options
		beachCannon.type = SiegeEngineType.BREACH_CANNON;
		beachCannon.itemName = Config.breachCannonItemName;
		beachCannon.itemLore = Config.breachCannonItemLore;
		beachCannon.shotAmount = Config.breachCannonShotAmount;
		beachCannon.velocityPerFuel = Config.breachCannonVelocityPerFuel;
		beachCannon.maxFuel = Config.breachCannonMaxFuel;
		beachCannon.fuelItem = new ItemStack(Config.breachCannonFuelItem);
		beachCannon.projectiles = Config.breachCannonProjectiles;
		beachCannon.placementOffsetY = -1;
		beachCannon.readyModelNumber = 142;
		beachCannon.modelNumberToFireAt = 142;
		beachCannon.firingModelNumbers = new ArrayList<Integer>();
		beachCannon.rotateStandHead = true;
		beachCannon.rotateSideways = false;
		definedSiegeEngines.put(beachCannon.readyModelNumber, beachCannon);
	}

}
