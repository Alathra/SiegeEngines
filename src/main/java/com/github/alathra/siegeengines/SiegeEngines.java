package com.github.alathra.siegeengines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.github.alathra.siegeengines.command.CommandHandler;
import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.crafting.CraftingRecipes;

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
		ClickHandler.items.clear();
		ClickHandler.items.add(new ItemStack(Material.ARMOR_STAND));
		addDefaults();
		for (SiegeEngine i : definedSiegeEngines.values()) {
			System.out.println("§eEnabled Weapon : " + i.getEngineName());
			System.out.println("§eWeapon Propellant/\"Fuel\" ItemStacks : " + i.getFuelItem());
			for (ItemStack proj : i.getProjectiles().keySet()) {
				System.out.println("§eWeapon Projectile ItemStacks : " + proj);
			}
		}
		commandHandler.onEnable();
		
		// load crafting recipes if enabled in config
		if (Config.craftingRecipes) {
			CraftingRecipes.loadCraftingRecipes();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onDisable() {
		commandHandler.onDisable();
		for (SiegeEngine siegeEngine : activeSiegeEngines.values()) {
			if (siegeEngine.getEntity() != null) {
				ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
				ItemMeta meta = item.getItemMeta();
				siegeEngine.setAmmoHolder(new SiegeEngineAmmoHolder());
				meta.setCustomModelData(siegeEngine.getReadyModelNumber());
				switch(siegeEngine.getType()) {
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

				((LivingEntity) siegeEngine.getEntity()).getEquipment().setHelmet(item);
			}
		}
	}

	public static void addDefaults() {

		// Trebuchet
		SiegeEngine trebuchet = new SiegeEngine("Trebuchet", Config.trebuchetProjectiles,
				new ItemStack(Material.STRING), 122);
		// config options
		trebuchet.setType(SiegeEngineType.TREBUCHET);
		trebuchet.setItemName(Config.trebuchetItemName);
		trebuchet.setItemLore(Config.trebuchetItemLore);
		trebuchet.setShotAmount(Config.trebuchetShotAmount);
		trebuchet.setVelocityPerFuel(Config.trebuchetVelocityPerFuel);
		trebuchet.setMaxFuel(Config.trebuchetMaxFuel);
		trebuchet.setFuelItem(new ItemStack(Config.trebuchetFuelItem));
		trebuchet.setProjectiles(Config.trebuchetProjectiles);
		trebuchet.setHealth(Config.trebuchetHealth);
		trebuchet.setXOffset(3);
		trebuchet.setYOffset(3);
		trebuchet.setPlacementOffsetY(0.0);
		trebuchet.setRotateStandHead(false);
		trebuchet.setRotateSideways(true);
		trebuchet.setReadyModelNumber(122);
		trebuchet.setModelNumberToFireAt(135);
		trebuchet.setMillisecondsBetweenFiringStages(2);
		trebuchet.setMillisecondsBetweenReloadingStages(10);
		trebuchet.setFiringModelNumbers(new ArrayList<>(
				Arrays.asList(123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139)));
		trebuchet.setCycleThroughModelsWhileFiring(true);
		trebuchet.setMountable(Config.trebuchetCanMount);
		definedSiegeEngines.put(trebuchet.getReadyModelNumber(), trebuchet);

		// Ballista
		SiegeEngine ballista = new SiegeEngine("Ballista", Config.ballistaProjectiles, new ItemStack(Material.STRING),
				145);
		// config options
		ballista.setType(SiegeEngineType.BALLISTA);
		ballista.setItemName(Config.ballistaItemName);
		ballista.setItemLore(Config.ballistaItemLore);
		ballista.setShotAmount(Config.ballistaShotAmount);
		ballista.setVelocityPerFuel(Config.ballistaVelocityPerFuel);
		ballista.setMaxFuel(Config.ballistaMaxFuel);
		ballista.setFuelItem(new ItemStack(Config.ballistaFuelItem));
		ballista.setProjectiles(Config.ballistaProjectiles);
		ballista.setHealth(Config.ballistaHealth);
		ballista.setXOffset(1);
		ballista.setYOffset(1);
		ballista.setPlacementOffsetY(-0.75);
		ballista.setRotateStandHead(true);
		ballista.setRotateSideways(true);
		ballista.setSetModelNumberWhenFullyLoaded(true);
		ballista.setReadyModelNumber(145);
		ballista.setModelNumberToFireAt(145);
		ballista.setPreFireModelNumber(143);
		ballista.setFiringModelNumbers(new ArrayList<>(Arrays.asList(143, 144, 145)));
		ballista.setCycleThroughModelsWhileFiring(true);
		ballista.setMountable(Config.ballistaCanMount);
		definedSiegeEngines.put(ballista.getReadyModelNumber(), ballista);

		// Siege Cannon
		SiegeEngine swivelCannon = new SiegeEngine("Swivel Cannon", Config.swivelCannonProjectiles,
				new ItemStack(Material.GUNPOWDER), 141);
		// config options
		swivelCannon.setType(SiegeEngineType.SWIVEL_CANNON);
		swivelCannon.setItemName(Config.swivelCannonItemName);
		swivelCannon.setItemLore(Config.swivelCannonItemLore);
		swivelCannon.setShotAmount(Config.swivelCannonShotAmount);
		swivelCannon.setVelocityPerFuel(Config.swivelCannonVelocityPerFuel);
		swivelCannon.setMaxFuel(Config.swivelCannonMaxFuel);
		swivelCannon.setFuelItem(new ItemStack(Config.swivelCannonFuelItem));
		swivelCannon.setProjectiles(Config.swivelCannonProjectiles);
		swivelCannon.setHealth(Config.swivelCannonHealth);
		swivelCannon.setPlacementOffsetY(-1);
		swivelCannon.setReadyModelNumber(141);
		swivelCannon.setModelNumberToFireAt(141);
		swivelCannon.setFiringModelNumbers(new ArrayList<Integer>());
		swivelCannon.setRotateStandHead(true);
		swivelCannon.setRotateSideways(true);
		swivelCannon.setMountable(Config.swivelCannonCanMount);
		definedSiegeEngines.put(swivelCannon.getReadyModelNumber(), swivelCannon);

		// Naval Cannon
		SiegeEngine breachCannon = new SiegeEngine("Breach Cannon", Config.breachCannonProjectiles,
				new ItemStack(Material.GUNPOWDER), 142);
		// config options
		breachCannon.setType(SiegeEngineType.BREACH_CANNON);
		breachCannon.setItemName(Config.breachCannonItemName);
		breachCannon.setItemLore(Config.breachCannonItemLore);
		breachCannon.setShotAmount(Config.breachCannonShotAmount);
		breachCannon.setVelocityPerFuel(Config.breachCannonVelocityPerFuel);
		breachCannon.setMaxFuel(Config.breachCannonMaxFuel);
		breachCannon.setFuelItem(new ItemStack(Config.breachCannonFuelItem));
		breachCannon.setProjectiles(Config.breachCannonProjectiles);
		breachCannon.setHealth(Config.breachCannonHealth);
		breachCannon.setPlacementOffsetY(-1);
		breachCannon.setReadyModelNumber(142);
		breachCannon.setModelNumberToFireAt(142);
		breachCannon.setFiringModelNumbers(new ArrayList<Integer>());
		breachCannon.setRotateStandHead(true);
		breachCannon.setRotateSideways(false);
		breachCannon.setMountable(Config.breachCannonCanMount);
		definedSiegeEngines.put(breachCannon.getReadyModelNumber(), breachCannon);

	}

}
