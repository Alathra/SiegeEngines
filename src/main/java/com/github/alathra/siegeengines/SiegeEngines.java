package com.github.alathra.siegeengines;

import com.github.alathra.siegeengines.command.CommandHandler;
import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.data.SiegeEnginesData;
import com.github.alathra.siegeengines.listeners.*;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValueAdapter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SiegeEngines extends JavaPlugin {

    private CommandHandler commandHandler;

    public static SiegeEngines instance;
    public static MetadataValueAdapter metadata;
    public static final Random random = new Random();

    // model id, defined siege engine types
    public static final HashMap<Integer, SiegeEngine> definedSiegeEngines = new HashMap<>();
    // SiegeEngine entity, SiegeEngine object
    public static final HashMap<UUID, SiegeEngine> activeSiegeEngines = new HashMap<>();
    // Player UUID, SiegeEngine entity
    public static final HashMap<UUID, List<Entity>> siegeEngineEntitiesPerPlayer = new HashMap<>();

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
        getServer().getPluginManager().registerEvents(new PlayerHandler(), this);
        getServer().getPluginManager().registerEvents(new SiegeEngineDamagedListener(), this);
        getServer().getPluginManager().registerEvents(new SiegeEngineDeathListener(), this);
        getServer().getPluginManager().registerEvents(new SiegeEngineFireListener(), this);
        getServer().getPluginManager().registerEvents(new SiegeEngineInteractListener(), this);
        getServer().getPluginManager().registerEvents(new SiegeEnginePlaceListener(), this);
        getServer().getPluginManager().registerEvents(new SiegeEngineRotationListener(), this);
        SiegeEnginesData.items.clear();
        SiegeEnginesData.items.add(new ItemStack(Material.ARMOR_STAND));
        addDefaults();
        if (Config.doDebug) {
            for (SiegeEngine i : definedSiegeEngines.values()) {
                SiegeEnginesLogger.info("Enabled Weapon : " + i.getEngineName());
                SiegeEnginesLogger.info("Weapon Propellant/\"Fuel\" ItemStacks : " + i.getFuelItem());
                for (ItemStack proj : i.getProjectiles().keySet()) {
                    SiegeEnginesLogger.info("Weapon Projectile ItemStacks : " + proj);
                }
            }
        }
        commandHandler.onEnable();
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
                switch (siegeEngine.getType()) {
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

                final EntityEquipment entityEquipment = ((LivingEntity) siegeEngine.getEntity()).getEquipment();
                if (entityEquipment == null)
                    continue;
                entityEquipment.setHelmet(item);
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
            146);
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
        ballista.setXOffset(2);
        ballista.setYOffset(1);
        ballista.setPlacementOffsetY(-1.25);
        ballista.setRotateStandHead(true);
        ballista.setRotateSideways(true);
        ballista.setSetModelNumberWhenFullyLoaded(true);
        ballista.setReadyModelNumber(146);
        ballista.setModelNumberToFireAt(146);
        ballista.setPreFireModelNumber(143);
        ballista.setPreLoadModelNumber(144);
        ballista.setFiringModelNumbers(new ArrayList<>(Arrays.asList(143, 144, 145, 146)));
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
        swivelCannon.setXOffset(3);
        swivelCannon.setPlacementOffsetY(-1);
        swivelCannon.setReadyModelNumber(141);
        swivelCannon.setModelNumberToFireAt(141);
        swivelCannon.setFiringModelNumbers(new ArrayList<>());
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
        breachCannon.setXOffset(3);
        breachCannon.setPlacementOffsetY(-1);
        breachCannon.setReadyModelNumber(142);
        breachCannon.setModelNumberToFireAt(142);
        breachCannon.setFiringModelNumbers(new ArrayList<>());
        breachCannon.setRotateStandHead(true);
        breachCannon.setRotateSideways(false);
        breachCannon.setMountable(Config.breachCannonCanMount);
        definedSiegeEngines.put(breachCannon.getReadyModelNumber(), breachCannon);

    }

}
