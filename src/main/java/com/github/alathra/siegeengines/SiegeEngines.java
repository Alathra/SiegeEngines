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
        AddDefaults();
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
                meta.setDisplayName("§e" + siegeEngine.name + " Item");
                List<String> Lore = new ArrayList<String>();
                Lore.add("§ePlace as a block to spawn a " + siegeEngine.name + " or put on an Armor Stand.");
                meta.setLore(Lore);
                item.setItemMeta(meta);

                ((LivingEntity) siegeEngine.entity).getEquipment().setHelmet(item);
            }
        }
    }

    public static void AddDefaults() {

        // Predefined projectiles with default values
        //SiegeEngineProjectile fireballShot = EntityProjectile.getDefaultFireballShot();
        //SiegeEngineProjectile stoneShot = ExplosiveProjectile.getDefaultStoneShot();
        //SiegeEngineProjectile repeatingShot = ExplosiveProjectile.getDefaultRepeatingShot();
        //SiegeEngineProjectile breachShot = ExplosiveProjectile.getDefaultBreachShot();
        //SiegeEngineProjectile scatterShot = EntityProjectile.getDefaultScatterShot();
        //SiegeEngineProjectile rocketShot = FireworkProjectile.getDefaultRocketShot(SiegeEnginesUtil.DEFAULT_ROCKET);

        // Trebuchet
        SiegeEngine trebuchet = new SiegeEngine("Trebuchet", Config.trebuchetProjectiles, new ItemStack(Material.STRING), 122);
        // config options
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
        trebuchet.firingModelNumbers = new ArrayList<>(Arrays.asList(
            123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139
        ));
        trebuchet.cycleThroughModelsBeforeFiring = true;
        definedSiegeEngines.put(trebuchet.readyModelNumber, trebuchet);

        // Ballista
        SiegeEngine ballista = new SiegeEngine("Ballista", Config.ballistaProjectiles, new ItemStack(Material.GUNPOWDER), 145);
        // config options
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
        ballista.readyModelNumber = 145;
        ballista.modelNumberToFireAt = 143;
        ballista.firingModelNumbers = new ArrayList<>(Arrays.asList(
            143, 144, 145
        ));
        ballista.cycleThroughModelsBeforeFiring = true;
        definedSiegeEngines.put(ballista.readyModelNumber, ballista);

        // Siege Cannon   
        SiegeEngine siegeCannon = new SiegeEngine("Siege Cannon", Config.siegeCannonProjectiles, new ItemStack(Material.GUNPOWDER), 141);
        // config options
        siegeCannon.shotAmount = Config.siegeCannonShotAmount;
        siegeCannon.velocityPerFuel = Config.siegeCannonVelocityPerFuel;
        siegeCannon.maxFuel = Config.siegeCannonMaxFuel;
        siegeCannon.fuelItem = new ItemStack(Config.siegeCannonFuelItem);
        siegeCannon.projectiles = Config.siegeCannonProjectiles;
        
        siegeCannon.placementOffsetY = -1;
        siegeCannon.readyModelNumber = 141;
        siegeCannon.modelNumberToFireAt = 141;
        siegeCannon.firingModelNumbers = new ArrayList<Integer>();
        siegeCannon.rotateStandHead = true;
        siegeCannon.rotateSideways = true;
        definedSiegeEngines.put(siegeCannon.readyModelNumber, siegeCannon);

        // Naval Cannon     
        SiegeEngine navalCannon = new SiegeEngine("Naval Cannon", Config.navalCannonProjectiles, new ItemStack(Material.GUNPOWDER), 142);
        // config options
        navalCannon.shotAmount = Config.navalCannonShotAmount;
        navalCannon.velocityPerFuel = Config.navalCannonVelocityPerFuel;
        navalCannon.maxFuel = Config.navalCannonMaxFuel;
        navalCannon.fuelItem = new ItemStack(Config.navalCannonFuelItem);
        navalCannon.projectiles = Config.navalCannonProjectiles;
        
        navalCannon.placementOffsetY = -1;
        navalCannon.readyModelNumber = 142;
        navalCannon.modelNumberToFireAt = 142;
        navalCannon.firingModelNumbers = new ArrayList<Integer>();
        navalCannon.rotateStandHead = true;
        navalCannon.rotateSideways = false;
        definedSiegeEngines.put(navalCannon.readyModelNumber, navalCannon);
    }

}
