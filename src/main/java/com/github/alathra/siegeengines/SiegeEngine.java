package com.github.alathra.siegeengines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.github.alathra.siegeengines.projectile.EntityProjectile;
import com.github.alathra.siegeengines.projectile.GunnersProjectile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    public EquipmentMagazine ammoHolder;
    
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
        ammoHolder = new EquipmentMagazine();
        //fuelItem = new ItemStack(Material.GUNPOWDER);
        cycleThroughModelsBeforeFiring = false;
        allowInvisibleStand = false;
        shotAmount = 1;
        ammoHolder.LoadedFuel = maxFuel;
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
        if (projectiles.keySet().isEmpty() || projectiles.isEmpty())
        	projectiles.put(new ItemStack(Material.GUNPOWDER), defaultProj);
        for (ItemStack mat : projectiles.keySet()) {
        	projectiles.put(mat, projectiles.get(mat));
        }
        
        // set passed parameters as object variables
        
        this.name = name;
    	this.projectiles = projectiles;
    	this.fuelItem = fuelItem;
    	this.customModelID = customModelID;
    }

    public boolean equals(String EquipmentId) {
        return id.equals(EquipmentId);
    }

    public Boolean isLoaded() {
        if (this.maxFuel <= 0 && ammoHolder.LoadedProjectile >= 1)
            return true;
        return (ammoHolder.LoadedFuel > 0 && ammoHolder.LoadedProjectile >= 1);
    }

    public Boolean CanLoadFuel() {
        return ammoHolder.LoadedFuel < maxFuel;
    }

    public Boolean LoadFuel(Entity player) {
        if (ammoHolder.LoadedFuel == maxFuel) {
            return true;
        }
        if (!(player instanceof Player)) {
            ammoHolder.LoadedFuel = maxFuel;
            return true;
        }
        if (((Player) player).getInventory().containsAtLeast(fuelItem, 1) || fuelItem.getType() == Material.AIR) {
            if (CanLoadFuel()) {
                this.ammoHolder.LoadedFuel += 1;
                //SaveState();
                ((Player) player).getInventory().removeItem(fuelItem);
                ((Player) player).sendMessage("§eLoaded " + ammoHolder.LoadedFuel + "/" + maxFuel);
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
            ammoHolder.LoadedProjectile = 1;
            ammoHolder.LoadedFuel = maxFuel;
            ammoHolder.MaterialName = itemInHand;
            return true;
        }
        if (projectiles.containsKey(itemInHand.getType()) && ammoHolder.LoadedProjectile == 0) {
            ammoHolder.LoadedProjectile = 1;
            ammoHolder.MaterialName = itemInHand;
            ((Player) player).sendMessage("§eAdding Ammunition to Weapon");
            itemInHand.setAmount(itemInHand.getAmount() - 1);
            return true;
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
                Location destination = origin.clone().add(direction);

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
        if (ammoHolder.LoadedFuel <= 0)
            ammoHolder.LoadedFuel = 0;
        float loadedFuel = ammoHolder.LoadedFuel;
        ItemStack LoadedProjectile = ammoHolder.MaterialName;


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
        ammoHolder.LoadedFuel = 0;
        ammoHolder.LoadedProjectile = 0;
        ammoHolder.MaterialName = new ItemStack(Material.AIR, 1);

        worldName = entity.getWorld().getName();
        nextShotTime = System.currentTimeMillis() + 1000;
        //for (int i = 0; i <= this.shotAmount /* range */; i += 1) {
            if (living == null || living.isDead()) {
                return;
            }
            if (cycleThroughModelsBeforeFiring) {

                this.taskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SiegeEngines.plugin, () -> {
                    if (living == null || living.isDead()) {
                        Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                        return;
                    }

                    if (hasFired) {
                        Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                        taskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SiegeEngines.plugin, () -> {
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
                                SiegeEngines.UpdateEntityIdModel(entity, readyModelNumber, worldName);
                                taskNumber = 0;
                            } else {
                                //firing stages
                                if (nextModelNumber - 1 <= firingModelNumbers.size() && nextModelNumber - 1 >= 0) {
                                    int modelData = firingModelNumbers.get(nextModelNumber - 1);
                                    //	player.sendMessage("§e" + modelData);
                                    SiegeEngines.UpdateEntityIdModel(entity, modelData, worldName);
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
                            SiegeEngines.UpdateEntityIdModel(entity, modelData, worldName);
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
                taskNumber = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SiegeEngines.plugin, () -> {
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
    //}

}
