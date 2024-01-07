package com.github.alathra.siegeengines.projectile;

import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.SiegeEnginesUtil;
import com.github.alathra.siegeengines.listeners.ClickHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ExplosiveProjectile extends SiegeEngineProjectile {

	
	// Defaults
    public Boolean placeBlocks = false;
    public Material blockToPlace = Material.COBWEB;
    public int blocksToPlaceAmount = 3;
    public float explodePower = 2;
    public float inaccuracy = 0.3f;
    public int projectilesCount = 1;
    public Boolean delayedFire = false;
    public int delayTime = 6;
    public Particle particleType = Particle.EXPLOSION_LARGE;
    public Sound soundType = Sound.ENTITY_GENERIC_EXPLODE;
    public Boolean alertOnLanding = false;
    
    private boolean playSound = true;
    
    public ExplosiveProjectile(ItemStack ammunitionItem) {
		super(ProjectileType.EXPLOSIVE, ammunitionItem);
	}
    
    public static ExplosiveProjectile getDefaultStoneShot() {
    	ExplosiveProjectile stoneProj = new ExplosiveProjectile(new ItemStack(Material.COBBLESTONE));
    	stoneProj.explodePower = 1;
    	return stoneProj;
    }
    
    public static ExplosiveProjectile getDefaultRepeatingShot() {
    	ExplosiveProjectile repeatingProj = new ExplosiveProjectile(new ItemStack(Material.TNT));
    	repeatingProj.explodePower = 1;
    	repeatingProj.projectilesCount = 5;
    	repeatingProj.delayedFire = true;
    	repeatingProj.inaccuracy = 0.75f;
    	return repeatingProj;
    }
    
    public static ExplosiveProjectile getDefaultBreachShot() {
    	ExplosiveProjectile breachProj = new ExplosiveProjectile(new ItemStack(Material.IRON_BLOCK));
    	breachProj.explodePower = 3;
    	breachProj.projectilesCount = 1;
    	return breachProj;
    }
    
    @Override
    public void Shoot(Entity player, Entity entity, Location loc, Float velocity) {
        playSound = true;
        int baseDelay = 0;
        for (int i = 0; i < projectilesCount; i++) {
            if (delayedFire) {
                baseDelay += delayTime;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SiegeEngines.getInstance(), () -> {
                    CreateEntity(entity, loc, velocity, player);
                }, (long) baseDelay);
            } else {
                CreateEntity(entity, loc, velocity, player);
            }
        }
    }

    private void CreateEntity(Entity entity, Location loc, Float velocity, Entity player) {
        World world = entity.getLocation().getWorld();
        Entity tnt = world.spawnEntity(loc, EntityType.SNOWBALL);
        Snowball ball = (Snowball) tnt;
        if (player instanceof org.bukkit.projectiles.ProjectileSource)
            ball.setShooter((org.bukkit.projectiles.ProjectileSource) player);
        ClickHandler.projectiles.put(tnt.getUniqueId(), this);

        if (inaccuracy != 0f) {
            tnt.setVelocity(loc.getDirection().multiply(velocity).add(Randomise())
                .subtract(Randomise()));
        } else {
            tnt.setVelocity(loc.getDirection().multiply(velocity));
        }
        tnt.setMetadata("isExplosiveProj", SiegeEnginesUtil.addMetaDataValue("true"));
        Bukkit.getServer().getPluginManager().callEvent(new org.bukkit.event.entity.ProjectileLaunchEvent(tnt));

        if (playSound) {
            world.playSound(loc, this.soundType, 20, 2);
            world.spawnParticle(this.particleType, loc.getX(), loc.getY(), loc.getZ(), 0);
            if (!delayedFire) {
                playSound = false;
            }
        }

    }

    private Vector Randomise() {
        return new Vector(SiegeEngines.random.nextFloat() * (inaccuracy - (inaccuracy * -1)) + (inaccuracy * -1), SiegeEngines.random.nextFloat() * (inaccuracy - (inaccuracy * -1)) + (inaccuracy * -1), SiegeEngines.random.nextFloat() * (inaccuracy - (inaccuracy * -1)) + (inaccuracy * -1));
    }

	public ProjectileType getProjectileType() {
		return projectileType;
	}
}


