package com.github.alathra.siegeengines.projectile;

import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.data.SiegeEnginesData;
import com.github.alathra.siegeengines.util.SiegeEnginesUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ExplosiveProjectile extends SiegeEngineProjectile {


    // Defaults
    public Boolean placeBlocks = false;
    public Material blockToPlace = Material.COBWEB; // TO-DO
    public int blocksToPlaceAmount = 3; // TO-DO
    public float explodePower = 2.0f;
    public float inaccuracy = 0.3f;
    public int projectilesCount = 1;
    public Boolean delayedFire = false;
    public int delayTime = 6;
    public Particle particleType = Particle.EXPLOSION_LARGE;
    public Sound soundType = Sound.ENTITY_GENERIC_EXPLODE;
    public Boolean alertOnLanding = false;
    public float velocityFactor = 1.0f;

    private boolean playSound = true;

    public ExplosiveProjectile(ItemStack ammunitionItem) {
        super(ProjectileType.EXPLOSIVE, ammunitionItem);
    }

    @Override
    public void Shoot(Entity player, Entity entity, Location loc, Float velocity) {
        playSound = true;
        int baseDelay = 0;
        for (int i = 0; i < projectilesCount; i++) {
            if (delayedFire) {
                baseDelay += delayTime;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SiegeEngines.getInstance(), () -> {
                    CreateEntity(entity, loc, velocity * velocityFactor, player);
                }, baseDelay);
            } else {
                CreateEntity(entity, loc, velocity * velocityFactor, player);
            }
        }
    }

    private void CreateEntity(Entity entity, Location loc, Float velocity, Entity player) {
        World world = entity.getLocation().getWorld();
        Entity tnt = world.spawnEntity(loc, EntityType.SNOWBALL);
        org.bukkit.entity.Projectile ball = (org.bukkit.entity.Projectile) tnt;
        if (player instanceof org.bukkit.projectiles.ProjectileSource)
            ball.setShooter((org.bukkit.projectiles.ProjectileSource) player);
        SiegeEnginesData.projectiles.put(tnt.getUniqueId(), this);

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


