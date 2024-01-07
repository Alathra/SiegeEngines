package com.github.alathra.siegeengines;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.UUID;

import com.github.alathra.siegeengines.projectile.EntityProjectile;
import com.github.alathra.siegeengines.projectile.GunnersProjectile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;


public class SiegeEngine implements Cloneable {
	
	// Core variables
	public Boolean enabled;
	public String id;
	public String worldName;
	public UUID entityId;
    public Entity entity;
    public int yOffset;
    public int xOffset;
    public int pitchOffset;
    public int maxFuel;
    public double placementOffsetY;
    public float velocityPerFuel;
	
	// Firing variables
    public int shotAmount;
    public long nextShotTime;
    public int millisecondsBetweenFiringStages;
    public int millisecondsBetweenReloadingStages;
    public int millisecondsToLoad;
    public int modelNumberToFireAt;
    public boolean cycleThroughModelsBeforeFiring;
    public boolean rotateSideways;
    public boolean rotateUpDown;
    public boolean rotateStandHead;
    public boolean hasFired;
    public boolean hasReloaded;
    public int taskNumber;
    public List<Integer> firingModelNumbers;
    public int readyModelNumber;
    public int nextModelNumber;
    public SiegeEngineAmmoHolder ammoHolder;
    
    // Optional variables
    public boolean allowInvisibleStand;
    public boolean hasBaseStand;
    public double baseStandOffset;
    public int baseStandModelNumber;
    
    // Passed parameters
    public String name = "Unnammed Siege Engine";
    public ItemStack fuelItem = new ItemStack(Material.GUNPOWDER);
    public HashMap<ItemStack, GunnersProjectile> projectiles;
	
    public int customModelID = 150;
    
    @Override
    public SiegeEngine clone() throws CloneNotSupportedException {
        return (SiegeEngine) super.clone();
    }
    
    public SiegeEngine(@NotNull String name, @NotNull HashMap<ItemStack, GunnersProjectile> projectiles, @NotNull ItemStack fuelItem, int customModelID) {
    	//String name, Integer XOffset, Integer YOffset, Integer fuelMax, Float fuelVelocityMod, Integer customModelId, HashMap<ItemStack, GunnersProjectile> projObj
        
        // Default custom model data id if it is passes as null
        if (customModelID == 0) {
            customModelID = 150;
        }
        
    	// Set Default values
        xOffset = 0;
        yOffset = 0;
        maxFuel = 5;
        velocityPerFuel = 1.0125f;
        id = name.replaceAll(" ", "_").toLowerCase();
        rotateStandHead = true;
        rotateSideways = true;
        placementOffsetY = -1.125f;
        readyModelNumber = customModelID;
        modelNumberToFireAt = customModelID;
        firingModelNumbers = new ArrayList<Integer>();
        ammoHolder = new SiegeEngineAmmoHolder();
        //fuelItem = new ItemStack(Material.GUNPOWDER);
        cycleThroughModelsBeforeFiring = false;
        allowInvisibleStand = false;
        shotAmount = 1;
        ammoHolder.loadedFuel = maxFuel;
        enabled = true;
        cycleThroughModelsBeforeFiring = false;
        rotateSideways = false;
        rotateUpDown = true;
        rotateStandHead = true;
        hasFired = false;
        hasReloaded = false;
        allowInvisibleStand = false;
        hasBaseStand = false;
        baseStandOffset = 0;
        baseStandModelNumber = 147;
        nextShotTime = System.currentTimeMillis();
        EntityProjectile defaultProj = new EntityProjectile();
        defaultProj.EntityCount = 2;
        defaultProj.EntityTyp = EntityType.SHULKER_BULLET;
        defaultProj.ParticleType = Particle.WHITE_ASH;
        defaultProj.SoundType = Sound.ENTITY_BLAZE_SHOOT;
        
        // set passed parameters as object variables
        this.name = name;
    	this.fuelItem = fuelItem;
    	this.customModelID = customModelID;
    	
        // set projectiles
        if (projectiles.keySet().isEmpty() || projectiles.isEmpty()) {
        	this.projectiles.put(new ItemStack(Material.GUNPOWDER), defaultProj);
        } else {
        	this.projectiles = projectiles;
        }
        
    }

    public boolean equals(String EquipmentId) {
        return id.equals(EquipmentId);
    }

    public Boolean isLoaded() {
        if (this.maxFuel <= 0 && ammoHolder.loadedProjectile >= 1)
            return true;
        return (ammoHolder.loadedFuel > 0 && ammoHolder.loadedProjectile >= 1);
    }

    public Boolean CanLoadFuel() {
        return ammoHolder.loadedFuel < maxFuel;
    }

    public Boolean LoadFuel(Entity player) {
        if (ammoHolder.loadedFuel == maxFuel) {
            return true;
        }
        if (!(player instanceof Player)) {
            ammoHolder.loadedFuel = maxFuel;
            return true;
        }
        if (((Player) player).getInventory().containsAtLeast(fuelItem, 1) || fuelItem.getType() == Material.AIR) {
            if (CanLoadFuel()) {
                this.ammoHolder.loadedFuel += 1;
                //SaveState();
                ((Player) player).getInventory().removeItem(fuelItem);
                ((Player) player).sendMessage("§eLoaded " + ammoHolder.loadedFuel + "/" + maxFuel);
                return true;
            } else {
                return false;
            }
        }

        return false;

    }

    public Boolean LoadProjectile(Entity player, ItemStack itemInHand) {
        if (itemInHand.getAmount() <= 0) {
            return false;
        }
        if (!(player instanceof Player)) {
            ammoHolder.loadedProjectile = 1;
            ammoHolder.loadedFuel = maxFuel;
            ammoHolder.materialName = itemInHand;
            return true;
        }
        for (ItemStack im : projectiles.keySet()) {
        	if(im.getType().equals(itemInHand.getType()) && ammoHolder.loadedProjectile == 0) {
        		ammoHolder.loadedProjectile = 1;
                ammoHolder.materialName = itemInHand;
                ((Player) player).sendMessage("§eAdding Ammunition to Weapon");
                itemInHand.setAmount(itemInHand.getAmount() - 1);
                return true;
        	}
        }
        return false;
    }

    public Location GetFireLocation(LivingEntity living) {
        if (living == null)
            return null;
        Location loc = living.getEyeLocation();
        Vector direction = living.getLocation().getDirection().multiply(xOffset);
        loc.add(direction);
        if (yOffset > 0) {
            loc.setY(loc.getY() + yOffset);
        } else {
            loc.setY(loc.getY() - yOffset);
        }
        loc.setPitch(loc.getPitch() + pitchOffset);
        return loc;
    }

    public void ShowFireLocation(Entity player) {
        LivingEntity living = (LivingEntity) entity;
        if (player instanceof Player) {
            if (living != null) {
                Location origin = this.GetFireLocation(living);
                Vector direction = origin.getDirection();
                direction.multiply(5 /* the range */);
                direction.normalize();
                for (int i = 0; i < 3 /* range */; i += 1) {
                    Location spawn = origin.add(direction);
                    ((Player) player).spawnParticle(Particle.CLOUD, spawn, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    public void Fire(Entity player, float delay, Integer amount) {
        if (amount == null || amount == 0)
            amount = this.shotAmount;
        this.shotAmount = amount;
        if (System.currentTimeMillis() < nextShotTime) {
            return;
        }
        if (ammoHolder.loadedFuel <= 0)
            ammoHolder.loadedFuel = 0;
        float loadedFuel = ammoHolder.loadedFuel;
        ItemStack LoadedProjectile = ammoHolder.materialName;


        LivingEntity living = (LivingEntity) entity;
        if (living == null || living.getEquipment() == null || living.getEquipment().getHelmet() == null || living.getEquipment().getHelmet().getItemMeta() == null) {
            return;
        }

        if (living.getEquipment().getHelmet().getItemMeta().getCustomModelData() != readyModelNumber) {
            if (player instanceof Player) {
                ((Player) player).sendMessage("§eCannot fire yet!");
            }
            return;
        }
        ammoHolder.loadedFuel = 0;
        ammoHolder.loadedProjectile = 0;
        ammoHolder.materialName = new ItemStack(Material.AIR, 1);

        worldName = entity.getWorld().getName();
        nextShotTime = System.currentTimeMillis() + 1000;
        //for (int i = 0; i <= this.shotAmount /* range */; i += 1) {
            if (living == null || living.isDead()) {
                return;
            }
            if (cycleThroughModelsBeforeFiring) {

                this.taskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SiegeEngines.getInstance(), () -> {
                    if (living == null || living.isDead()) {
                        Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                        return;
                    }

                    if (hasFired) {
                        Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                        taskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SiegeEngines.getInstance(), () -> {
                            if (living == null || living.isDead()) {
                                Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                                return;
                            }
                            //	player.sendMessage("§etask");
                            if (hasReloaded) {
                                Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                                hasReloaded = false;
                                hasFired = false;
                                nextModelNumber = 0;
                                SiegeEnginesUtil.UpdateEntityIdModel(entity, readyModelNumber, worldName);
                                taskNumber = 0;
                            } else {
                                //firing stages
                                if (nextModelNumber - 1 <= firingModelNumbers.size() && nextModelNumber - 1 >= 0) {
                                    int modelData = firingModelNumbers.get(nextModelNumber - 1);
                                    //	player.sendMessage("§e" + modelData);
                                    SiegeEnginesUtil.UpdateEntityIdModel(entity, modelData, worldName);
                                    nextModelNumber -= 1;

                                } else {
                                    //	plugin.getLogger().log(Level.INFO, "its reloaded");
                                    hasReloaded = true;
                                }
                            }
                        }, 0, millisecondsBetweenReloadingStages);

                    } else {
                        //firing stages
                        if (nextModelNumber < firingModelNumbers.size()) {

                            int modelData = firingModelNumbers.get(nextModelNumber);
                            //	player.sendMessage("§e" + modelData);
                            SiegeEnginesUtil.UpdateEntityIdModel(entity, modelData, worldName);
                            if (modelData == modelNumberToFireAt) {
                                //	player.sendMessage("§efiring" + modelData);
                                if (LoadedProjectile == null) return;
                                if (LoadedProjectile.getType() == Material.AIR) return;
                                GunnersProjectile projType = projectiles.get(LoadedProjectile);
                                if (projType == null) return;
                                projType.Shoot(player, entity, this.GetFireLocation(living), loadedFuel * velocityPerFuel);
                            }
                            nextModelNumber += 1;

                        } else {
                            hasFired = true;

                        }
                    }
                }, 0, millisecondsBetweenFiringStages);
            } else {
                taskNumber = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SiegeEngines.getInstance(), () -> {
                    //player.sendMessage("§etask");
                    if (living == null || living.isDead()) {
                        Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                        return;
                    }

                    Location loc = living.getEyeLocation();
                    Vector direction = entity.getLocation().getDirection().multiply(xOffset);

                    loc.add(direction);

                    nextModelNumber = 0;
                    if (LoadedProjectile == null) return;
                    if (LoadedProjectile.getType() == Material.AIR) return;
                    GunnersProjectile projType = projectiles.get(LoadedProjectile);
                    if (projType == null) return;
                    projType.Shoot(player, entity, this.GetFireLocation(living), loadedFuel * velocityPerFuel);
                }, (long) delay);

            }
        }
    
    @SuppressWarnings("deprecation")
	public Boolean place(Entity player, Location l) {
        l.add(0.5, 0, 0.5);
        NamespacedKey key = new NamespacedKey(SiegeEngines.getInstance(), "siege_engines");
        if (this == null || !this.enabled) {
            return false;
        }
        l.setY(l.getY() + 1);
        l.setDirection(player.getFacing().getDirection());
        ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
        ItemMeta meta = item.getItemMeta();
        String id = "";
        this.ammoHolder = new SiegeEngineAmmoHolder();
        if (this.hasBaseStand) {

            Location l2 = l;
            l2.setY(l.getY() + this.baseStandOffset);
            Entity entity3 = player.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);

            LivingEntity ent = (LivingEntity) entity3;
            ArmorStand stand = (ArmorStand) ent;
            id = entity3.getUniqueId().toString();
            meta.setCustomModelData(this.baseStandModelNumber);
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
            stand.setInvisible(this.allowInvisibleStand);
            ent.getEquipment().setHelmet(item);
            stand.setGravity(false);
            stand.setMarker(true);
        }
        l.setY(l.getY() + this.placementOffsetY);
        Entity entity2 = player.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        if (id != "") {
            entity2.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        }
        meta.setCustomModelData(this.readyModelNumber);
        meta.setDisplayName("§e" + this.name + " Item");
        List<String> Lore = new ArrayList<String>();
        Lore.add("§ePlace as a block to spawn a " + this.name + " or put on an Armor Stand.");
        meta.setLore(Lore);
        item.setItemMeta(meta);

        //	entity3.addPassenger(entity2);

        LivingEntity ent = (LivingEntity) entity2;
        ArmorStand stand = (ArmorStand) ent;
        this.entity = entity2;

        this.entityId = entity2.getUniqueId();
        stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.REMOVING_OR_CHANGING);
        stand.setInvisible(this.allowInvisibleStand);
        stand.setBasePlate(true);

        stand.setGravity(false);
        ent.getEquipment().setHelmet(item);
        if (SiegeEngines.trackedStands.containsKey(player.getUniqueId())) {
            List<Entity> entities = SiegeEngines.trackedStands.get(player.getUniqueId());
            entities.add(entity2);
            SiegeEngines.trackedStands.put(player.getUniqueId(), entities);
        } else {
            List<Entity> newList = new ArrayList<Entity>();
            newList.add(entity2);
            SiegeEngines.trackedStands.put(player.getUniqueId(), newList);
        }
        SiegeEngines.activeSiegeEngines.put(entity2.getUniqueId(), this);
        return true;
    }
}
