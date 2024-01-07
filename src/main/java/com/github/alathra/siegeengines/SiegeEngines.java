package com.github.alathra.siegeengines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.github.alathra.siegeengines.command.CommandHandler;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.metadata.MetadataValueAdapter;

import com.github.alathra.siegeengines.listeners.ClickHandler;
import com.github.alathra.siegeengines.listeners.RotationHandler;
import com.github.alathra.siegeengines.projectile.EntityProjectile;
import com.github.alathra.siegeengines.projectile.ExplosiveProjectile;
import com.github.alathra.siegeengines.projectile.GunnersProjectile;


public class SiegeEngines extends JavaPlugin {
	
	private CommandHandler commandHandler;
	
	public static SiegeEngines instance;
    public static MetadataValueAdapter metadata;
    public static Random random = new Random();
    
    public static HashMap<Integer, SiegeEngine> definedSiegeEngines = new HashMap<Integer, SiegeEngine>();
    public static HashMap<UUID, SiegeEngine> activeSiegeEngines = new HashMap<UUID, SiegeEngine>();
    public static HashMap<UUID, List<Entity>> trackedStands = new HashMap<UUID, List<Entity>>();
   

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
        trackedStands.clear();
        definedSiegeEngines.clear();
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
        HashMap<ItemStack, GunnersProjectile> projectileMap =  new HashMap<>();

        EntityProjectile fireProj = new EntityProjectile();
        fireProj.EntityCount = 4;
        fireProj.EntityTyp = EntityType.SMALL_FIREBALL;
        fireProj.ParticleType = Particle.WHITE_ASH;
        fireProj.SoundType = Sound.ENTITY_BLAZE_SHOOT;
        fireProj.Inaccuracy = 0.75f;
        projectileMap.put(new ItemStack(Material.FIRE_CHARGE), fireProj);

        ExplosiveProjectile proj = new ExplosiveProjectile();
        proj.ExplodePower = 1;
        projectileMap.put(new ItemStack(Material.COBBLESTONE), proj);

    	SiegeEngine trebuchet = new SiegeEngine("Trebuchet", projectileMap, new ItemStack(Material.GUNPOWDER), 122);
    	trebuchet.xOffset = 3;
    	trebuchet.yOffset = 3;
        trebuchet.placementOffsetY = 0.0;
    	trebuchet.velocityPerFuel = 0.3f;
        trebuchet.shotAmount = 1;
        trebuchet.rotateStandHead = false;
        trebuchet.rotateSideways = true;
        trebuchet.fuelItem = new ItemStack(Material.STRING);
        trebuchet.readyModelNumber = 122;
        trebuchet.modelNumberToFireAt = 135;
        trebuchet.millisecondsBetweenFiringStages = 2;
        trebuchet.millisecondsBetweenReloadingStages = 10;
        trebuchet.firingModelNumbers = new ArrayList<>(Arrays.asList(
            123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139
        ));
        trebuchet.cycleThroughModelsBeforeFiring = true;

        definedSiegeEngines.put(trebuchet.readyModelNumber, trebuchet);


        ExplosiveProjectile tntProj = new ExplosiveProjectile();
        tntProj.ExplodePower = 1;
        tntProj.ProjectilesCount = 5;
        tntProj.DelayedFire = true;
        tntProj.Inaccuracy = 0.75f;
        projectileMap.put(new ItemStack(Material.TNT), tntProj);

        ExplosiveProjectile breachShot = new ExplosiveProjectile();
        breachShot.ExplodePower = 3;
        breachShot.ProjectilesCount = 1;
        projectileMap.put(new ItemStack(Material.IRON_BLOCK), breachShot);


        EntityProjectile scatterShot = new EntityProjectile();
        scatterShot.Inaccuracy = 0.5f;
        scatterShot.EntityCount = 24;
        scatterShot.ParticleType = Particle.ELECTRIC_SPARK;
        scatterShot.SoundType = Sound.ITEM_CROSSBOW_SHOOT;
        projectileMap.put(new ItemStack(Material.GRAVEL), scatterShot);

        ExplosiveProjectile repeatingShot = new ExplosiveProjectile();
        repeatingShot.ExplodePower = 2;
        repeatingShot.ProjectilesCount = 3;
        repeatingShot.DelayedFire = true;
        repeatingShot.Inaccuracy = 0.35f;
        projectileMap.put(new ItemStack(Material.TNT), repeatingShot);
        
        SiegeEngine siegeCannon = new SiegeEngine("Siege Cannon", projectileMap, new ItemStack(Material.GUNPOWDER), 141);
        siegeCannon.shotAmount = 1;
        siegeCannon.placementOffsetY = -1;
        siegeCannon.readyModelNumber = 141;
        siegeCannon.modelNumberToFireAt = 141;
        siegeCannon.firingModelNumbers = new ArrayList<Integer>();
        siegeCannon.rotateStandHead = true;
        siegeCannon.rotateSideways = true;
        definedSiegeEngines.put(siegeCannon.readyModelNumber, siegeCannon);

        SiegeEngine navalCannon = new SiegeEngine("Naval Cannon", projectileMap, new ItemStack(Material.GUNPOWDER), 142);
        navalCannon.projectiles.put(new ItemStack(Material.COBBLESTONE), proj);
        navalCannon.placementOffsetY = -1;
        navalCannon.shotAmount = 1;
        navalCannon.readyModelNumber = 142;
        navalCannon.modelNumberToFireAt = 142;
        navalCannon.firingModelNumbers = new ArrayList<Integer>();
        navalCannon.rotateStandHead = true;
        navalCannon.rotateSideways = false;
        definedSiegeEngines.put(navalCannon.readyModelNumber, navalCannon);
    }

}