package com.github.alathra.siegeengines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.alathra.siegeengines.command.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.bukkit.metadata.MetadataValueAdapter;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.alathra.siegeengines.listeners.ClickHandler;
import com.github.alathra.siegeengines.listeners.RotationHandler;
import com.github.alathra.siegeengines.projectile.EntityProjectile;
import com.github.alathra.siegeengines.projectile.ExplosiveProjectile;
import com.github.alathra.siegeengines.projectile.GunnersProjectile;


public class SiegeEngines extends JavaPlugin {
	
	public static SiegeEngines instance;
	
    public static Plugin plugin;
    public static MetadataValueAdapter metadata;
    public static Random random = new Random();
    private static String Path;
    private CommandHandler commandHandler;
   

	public static SiegeEngines getInstance() {
		return instance;
	}
	
    @Override
    public void onLoad() {
        plugin = this;
        commandHandler = new CommandHandler(this);
        commandHandler.onLoad();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
    	instance = this;
        Path = this.getDataFolder().getAbsolutePath();
        getServer().getPluginManager().registerEvents(new RotationHandler(), this);
        getServer().getPluginManager().registerEvents(new ClickHandler(), this);
        equipment.clear();
        TrackedStands.clear();
        DefinedEquipment.clear();
        AddDefaults();
        for (SiegeEngine i : DefinedEquipment.values()) {
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
        for (SiegeEngine siegeEngine : equipment.values()) {
            if (siegeEngine.entity != null) {
                ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
                ItemMeta meta = item.getItemMeta();
                siegeEngine.ammoHolder = new EquipmentMagazine();

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

    public static FixedMetadataValue addMetaDataValue(Object value) {
        return new FixedMetadataValue(Bukkit.getServer().getPluginManager().getPlugin("SiegeEngines"), value);
    }

    public static SiegeEngine CreateClone(Integer ModelId) {
        try {
            return DefinedEquipment.get(ModelId).clone();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block

        }
        return null;
    }

    public static HashMap<Integer, SiegeEngine> DefinedEquipment = new HashMap<Integer, SiegeEngine>();

    public static HashMap<UUID, List<Entity>> TrackedStands = new HashMap<UUID, List<Entity>>();

    public static HashMap<UUID, SiegeEngine> equipment = new HashMap<UUID, SiegeEngine>();

    public static String convertTime(long time) {

        long days = TimeUnit.MILLISECONDS.toDays(time);
        time -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(time);
        time -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
        String timeLeftFormatted = String.format("§e" + minutes + " Minutes " + seconds + " Seconds§f");

        return timeLeftFormatted;
    }

    public static void AddDefaults() {
    	//String name, Integer XOffset, Integer YOffset, Integer fuelMax, Float fuelVelocityMod, Integer customModelId, HashMap<ItemStack, GunnersProjectile> projObj
        //@NotNull String name, @NotNull HashMap<ItemStack, GunnersProjectile> projectiles, @NotNull ItemStack fuelItem, int customModelID
    	SiegeEngine trebuchet = new SiegeEngine("Trebuchet", new HashMap<ItemStack, GunnersProjectile>(), new ItemStack(Material.GUNPOWDER), 150);
    	trebuchet.xOffset = 5;
    	trebuchet.yOffset = 5;
    	trebuchet.velocityPerFuel = 0.3f;
        ExplosiveProjectile proj = new ExplosiveProjectile();
        proj.ExplodePower = 1;
        trebuchet.shotAmount = 2;
        trebuchet.rotateStandHead = false;
        trebuchet.rotateSideways = true;
        trebuchet.fuelItem = new ItemStack(Material.STRING);
        trebuchet.projectiles.put(new ItemStack(Material.COBBLESTONE), proj);
        trebuchet.readyModelNumber = 122;
        trebuchet.modelNumberToFireAt = 135;
        trebuchet.millisecondsBetweenFiringStages = 2;
        trebuchet.millisecondsBetweenReloadingStages = 10;
        trebuchet.firingModelNumbers = new ArrayList<>(Arrays.asList(
            123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139
        ));
        trebuchet.cycleThroughModelsBeforeFiring = true;
        DefinedEquipment.put(trebuchet.readyModelNumber, trebuchet);

        SiegeEngine navalCannon = new SiegeEngine("Naval Cannon", new HashMap<ItemStack, GunnersProjectile>(), new ItemStack(Material.GUNPOWDER), 142);
        navalCannon.projectiles.put(new ItemStack(Material.COBBLESTONE), proj);
        navalCannon.placementOffsetY = -1;
        navalCannon.shotAmount = 1;
        navalCannon.readyModelNumber = 142;
        navalCannon.modelNumberToFireAt = 142;
        navalCannon.firingModelNumbers = new ArrayList<Integer>();
        navalCannon.rotateStandHead = true;
        navalCannon.rotateSideways = false;
        proj = new ExplosiveProjectile();
        proj.ExplodePower = 1;
        proj.ProjectilesCount = 5;
        proj.DelayedFire = true;
        proj.Inaccuracy = 0.75f;
        navalCannon.projectiles.put(new ItemStack(Material.TNT), proj);
        proj = new ExplosiveProjectile();
        proj.ExplodePower = 3;
        proj.ProjectilesCount = 1;
        navalCannon.projectiles.put(new ItemStack(Material.IRON_BLOCK), proj);

        EntityProjectile fireProj = new EntityProjectile();
        fireProj.EntityCount = 4;
        fireProj.EntityTyp = EntityType.SMALL_FIREBALL;
        fireProj.ParticleType = Particle.WHITE_ASH;
        fireProj.SoundType = Sound.ENTITY_BLAZE_SHOOT;
        fireProj.Inaccuracy = 0.75f;
        navalCannon.projectiles.put(new ItemStack(Material.FIRE_CHARGE), fireProj);
        DefinedEquipment.put(navalCannon.readyModelNumber, navalCannon);
        
        SiegeEngine siegeCannon = new SiegeEngine("Siege Cannon", new HashMap<ItemStack, GunnersProjectile>(), new ItemStack(Material.GUNPOWDER), 141);
        proj.ExplodePower = 2;
        siegeCannon.projectiles.put(new ItemStack(Material.COBBLESTONE), proj);
        siegeCannon.shotAmount = 1;
        siegeCannon.placementOffsetY = -1;
        siegeCannon.readyModelNumber = 141;
        siegeCannon.modelNumberToFireAt = 141;
        siegeCannon.firingModelNumbers = new ArrayList<Integer>();
        siegeCannon.rotateStandHead = true;
        siegeCannon.rotateSideways = true;
        proj = new ExplosiveProjectile();
        proj.ExplodePower = 2;
        proj.ProjectilesCount = 3;
        proj.DelayedFire = true;
        proj.Inaccuracy = 0.75f;
        siegeCannon.projectiles.put(new ItemStack(Material.TNT), proj);
        proj = new ExplosiveProjectile();
        proj.ExplodePower = 3;
        proj.ProjectilesCount = 1;
        siegeCannon.projectiles.put(new ItemStack(Material.IRON_BLOCK), proj);
        EntityProjectile scatterShot = new EntityProjectile();
        scatterShot.Inaccuracy = 0.5f;
        scatterShot.EntityCount = 24;
        scatterShot.ParticleType = Particle.ELECTRIC_SPARK;
        scatterShot.SoundType = Sound.ITEM_CROSSBOW_SHOOT;
        siegeCannon.projectiles.put(new ItemStack(Material.GRAVEL), scatterShot);
        DefinedEquipment.put(siegeCannon.readyModelNumber, siegeCannon);
    }

    public static Boolean placeEquipment(Entity player, int CustomModelData, Location l) {
        l.add(0.5, 0, 0.5);
        NamespacedKey key = new NamespacedKey(SiegeEngines.plugin, "cannons");
        SiegeEngine siegeEngine = CreateClone(CustomModelData);
        if (siegeEngine == null || !siegeEngine.enabled) {
            return false;
        }
        l.setY(l.getY() + 1);
        int maxNearby = 0;
        l.setDirection(player.getFacing().getDirection());
        ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
        ItemMeta meta = item.getItemMeta();
        String id = "";
        siegeEngine.ammoHolder = new EquipmentMagazine();
        if (siegeEngine.hasBaseStand) {

            Location l2 = l;
            l2.setY(l.getY() + siegeEngine.baseStandOffset);
            Entity entity3 = player.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);

            LivingEntity ent = (LivingEntity) entity3;
            ArmorStand stand = (ArmorStand) ent;
            id = entity3.getUniqueId().toString();
            meta.setCustomModelData(siegeEngine.baseStandModelNumber);
            stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.REMOVING_OR_CHANGING);
            stand.setBasePlate(false);
            //	stand.setSmall(true);
            item.setItemMeta(meta);
            stand.setInvisible(siegeEngine.allowInvisibleStand);
            ent.getEquipment().setHelmet(item);
            stand.setGravity(false);
            stand.setMarker(true);
        }
        l.setY(l.getY() + siegeEngine.placementOffsetY);
        Entity entity2 = player.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        if (id != "") {
            entity2.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        }
        meta.setCustomModelData(siegeEngine.readyModelNumber);
        meta.setDisplayName("§e" + siegeEngine.name + " Item");
        List<String> Lore = new ArrayList<String>();
        Lore.add("§ePlace as a block to spawn a " + siegeEngine.name + " or put on an Armor Stand.");
        meta.setLore(Lore);
        item.setItemMeta(meta);

        //	entity3.addPassenger(entity2);

        LivingEntity ent = (LivingEntity) entity2;
        ArmorStand stand = (ArmorStand) ent;
        siegeEngine.entity = entity2;

        siegeEngine.entityId = entity2.getUniqueId();
        stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.REMOVING_OR_CHANGING);
        stand.setInvisible(siegeEngine.allowInvisibleStand);
        stand.setBasePlate(true);

        stand.setGravity(false);
        ent.getEquipment().setHelmet(item);
        if (TrackedStands.containsKey(player.getUniqueId())) {
            List<Entity> entities = TrackedStands.get(player.getUniqueId());
            entities.add(entity2);
            TrackedStands.put(player.getUniqueId(), entities);
        } else {
            List<Entity> newList = new ArrayList<Entity>();
            newList.add(entity2);
            TrackedStands.put(player.getUniqueId(), newList);
        }
        equipment.put(entity2.getUniqueId(), siegeEngine);
        return true;
    }

    public static void UpdateEntityIdModel(Entity ent, int modelNumber, String WorldName) {
        if (ent instanceof LivingEntity) {
            LivingEntity liv = (LivingEntity) ent;
            ItemStack Helmet = liv.getEquipment().getHelmet();
            if (Helmet != null) {
                ItemMeta meta = Helmet.getItemMeta();
                meta.setCustomModelData(modelNumber);
                Helmet.setItemMeta(meta);
                liv.getEquipment().setHelmet(Helmet);
            }
        }
    }

}



