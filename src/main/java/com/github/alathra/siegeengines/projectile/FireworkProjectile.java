package com.github.alathra.siegeengines.projectile;

import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.SiegeEnginesUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class FireworkProjectile extends SiegeEngineProjectile {
	
	// Defaults 
	public int entityCount = 1;
    public int arrowOnlyDamage = 6;
    public Boolean delayedFire = true;
    public int delayTime = 6;
    public EntityType entityType = EntityType.FIREWORK;
    public float inaccuracy = 0.125f;
    public Particle particleType = Particle.EXPLOSION_NORMAL;
    public Sound soundType = Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR;
    
    private boolean playSound = true;
    
    public FireworkProjectile(ItemStack ammunitionItem) {
		super(ProjectileType.EXPLOSIVE, ammunitionItem);
		
		// Defaults
	}
    
    public static FireworkProjectile getDefaultRocketShot() {
    	FireworkProjectile fireProj = new FireworkProjectile(new ItemStack(Material.FIREWORK_ROCKET));
        fireProj.entityCount = 1;
        fireProj.entityType = EntityType.FIREWORK;
        fireProj.particleType = Particle.WHITE_ASH;
        fireProj.soundType = Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR;
        fireProj.inaccuracy = 0.125f;
        return fireProj;
    }

    @Override
    public void Shoot(Entity player, Entity entity, Location FireLocation, Float velocity) {
        playSound = true;
        int baseDelay = 0;
        for (int i = 0; i < entityCount; i++) {
            if (delayedFire) {
                baseDelay += delayTime;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SiegeEngines.getInstance(), () -> {
                    CreateEntity(entity, FireLocation, velocity, player);
                }, (long) baseDelay);
            } else {
                CreateEntity(entity, FireLocation, velocity, player);
            }
        }
    }

    private void CreateEntity(Entity entity, Location loc, Float velocity, Entity player) {
        World world = entity.getLocation().getWorld();
        
        Entity arrow = world.spawnEntity(loc, entityType);
        if (inaccuracy != 0f) {
            arrow.setVelocity(loc.getDirection().multiply(velocity).add(Randomise())
                .subtract(Randomise()));
        } else {
            arrow.setVelocity(loc.getDirection().multiply(velocity));
        }
        arrow.setMetadata("isRocketProj", SiegeEnginesUtil.addMetaDataValue("true"));
        Bukkit.getServer().getPluginManager().callEvent(new org.bukkit.event.entity.ProjectileLaunchEvent(arrow));

        if (arrow instanceof org.bukkit.entity.Projectile) {
            if (player instanceof org.bukkit.projectiles.ProjectileSource)
                ((org.bukkit.entity.Projectile) arrow).setShooter((org.bukkit.projectiles.ProjectileSource) player);
        }
        if (arrow instanceof Firework) {
            Firework firework = (Firework) arrow;
            if (player instanceof org.bukkit.projectiles.ProjectileSource) {
                firework.setShotAtAngle(true);
                firework.setShooter((org.bukkit.projectiles.ProjectileSource) player);
                ItemStack rocketItem = getAmmuinitionItem();
                if (rocketItem.getItemMeta() instanceof FireworkMeta) {
                    firework.setFireworkMeta((FireworkMeta)rocketItem.getItemMeta());
                }
                int itemPower = 1+firework.getFireworkMeta().getPower();
                itemPower *= firework.getMaxLife();
                firework.setMaxLife(itemPower);
            }
        }
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