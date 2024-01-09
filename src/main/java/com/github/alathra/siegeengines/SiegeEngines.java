package com.github.alathra.siegeengines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.github.alathra.siegeengines.command.CommandHandler;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.metadata.MetadataValueAdapter;

import com.github.alathra.siegeengines.listeners.ClickHandler;
import com.github.alathra.siegeengines.listeners.RotationHandler;
import com.github.alathra.siegeengines.projectile.EntityProjectile;
import com.github.alathra.siegeengines.projectile.FireworkProjectile;
import com.github.alathra.siegeengines.projectile.ExplosiveProjectile;
import com.github.alathra.siegeengines.projectile.SiegeEngineProjectile;


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
        getServer().getPluginManager().registerEvents(new RotationHandler(), this);
        getServer().getPluginManager().registerEvents(new ClickHandler(), this);
        activeSiegeEngines.clear();
        siegeEngineEntitiesPerPlayer.clear();
        definedSiegeEngines.clear();
        AddDefaults();
        for (SiegeEngine i : definedSiegeEngines.values()) {
            System.out.println("§eEnabled Weapon : " + i.name);
            System.out.println("§eWeapon Propellant/\"Fuel\" ItemStacks : " + i.fuelItem);
            for (ItemStack proj : i.projectiles.keySet()) {
                System.out.println("§eWeapon Projectile ItemStacks : " + proj);
            }
        }
        this.saveDefaultConfig();
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
        SiegeEngineProjectile fireballShot = EntityProjectile.getDefaultFireballShot();
        SiegeEngineProjectile stoneShot = ExplosiveProjectile.getDefaultStoneShot();
        SiegeEngineProjectile repeatingShot = ExplosiveProjectile.getDefaultRepeatingShot();
        SiegeEngineProjectile breachShot = ExplosiveProjectile.getDefaultBreachShot();
        SiegeEngineProjectile scatterShot = EntityProjectile.getDefaultScatterShot();
        SiegeEngineProjectile rocketShot = FireworkProjectile.getDefaultRocketShot(SiegeEnginesUtil.DEFAULT_ROCKET);
        
        // Trebuchet
        HashMap<ItemStack, SiegeEngineProjectile> trebuchetProjectiles = new HashMap<>();
        trebuchetProjectiles.put(stoneShot.getAmmuinitionItem(), stoneShot);
        trebuchetProjectiles.put(fireballShot.getAmmuinitionItem(), fireballShot);
        trebuchetProjectiles.put(breachShot.getAmmuinitionItem(), breachShot);
        
        SiegeEngine trebuchet = new SiegeEngine("Trebuchet", trebuchetProjectiles, new ItemStack(Material.STRING), 122);
        trebuchet.shotAmount = 1;
        trebuchet.xOffset = 3;
        trebuchet.yOffset = 3;
        trebuchet.placementOffsetY = 0.0;
        trebuchet.velocityPerFuel = 0.3f;
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
        trebuchet.projectiles = trebuchetProjectiles;
        definedSiegeEngines.put(trebuchet.readyModelNumber, trebuchet);
        
        // Siege Cannon
        HashMap<ItemStack, SiegeEngineProjectile> siegeCannonProjectiles = new HashMap<>();
        siegeCannonProjectiles.put(stoneShot.getAmmuinitionItem(), stoneShot);
        siegeCannonProjectiles.put(fireballShot.getAmmuinitionItem(), fireballShot);
        siegeCannonProjectiles.put(breachShot.getAmmuinitionItem(), breachShot);
        siegeCannonProjectiles.put(repeatingShot.getAmmuinitionItem(), repeatingShot);
        siegeCannonProjectiles.put(scatterShot.getAmmuinitionItem(), scatterShot);
        siegeCannonProjectiles.put(rocketShot.getAmmuinitionItem(), rocketShot);
        
        SiegeEngine siegeCannon = new SiegeEngine("Siege Cannon", siegeCannonProjectiles, new ItemStack(Material.GUNPOWDER), 141);
        siegeCannon.shotAmount = 1;
        siegeCannon.placementOffsetY = -1;
        siegeCannon.readyModelNumber = 141;
        siegeCannon.modelNumberToFireAt = 141;
        siegeCannon.firingModelNumbers = new ArrayList<Integer>();
        siegeCannon.rotateStandHead = true;
        siegeCannon.rotateSideways = true;
        siegeCannon.projectiles = siegeCannonProjectiles;
        definedSiegeEngines.put(siegeCannon.readyModelNumber, siegeCannon);

        // Naval Cannon
        HashMap<ItemStack, SiegeEngineProjectile> navalCannonProjectiles = new HashMap<>();
        navalCannonProjectiles.put(stoneShot.getAmmuinitionItem(), stoneShot);
        navalCannonProjectiles.put(fireballShot.getAmmuinitionItem(), fireballShot);
        navalCannonProjectiles.put(breachShot.getAmmuinitionItem(), breachShot);
        navalCannonProjectiles.put(repeatingShot.getAmmuinitionItem(), repeatingShot);
        navalCannonProjectiles.put(scatterShot.getAmmuinitionItem(), scatterShot);
        navalCannonProjectiles.put(rocketShot.getAmmuinitionItem(), rocketShot);
        
        SiegeEngine navalCannon = new SiegeEngine("Naval Cannon", navalCannonProjectiles, new ItemStack(Material.GUNPOWDER), 142);
        navalCannon.shotAmount = 1;
        navalCannon.placementOffsetY = -1;
        navalCannon.readyModelNumber = 142;
        navalCannon.modelNumberToFireAt = 142;
        navalCannon.firingModelNumbers = new ArrayList<Integer>();
        navalCannon.rotateStandHead = true;
        navalCannon.rotateSideways = false;
        navalCannon.projectiles = navalCannonProjectiles;
        definedSiegeEngines.put(navalCannon.readyModelNumber, navalCannon);
    }

}