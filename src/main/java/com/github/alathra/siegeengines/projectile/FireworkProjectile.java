package com.github.alathra.siegeengines.projectile;

import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.util.SiegeEnginesUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class FireworkProjectile extends SiegeEngineProjectile {

    // Defaults
    public int projectileCount = 1;
    public Boolean delayedFire = true;
    public int delayTime = 6;
    public EntityType entityType = EntityType.FIREWORK_ROCKET;
    public float inaccuracy = 0.125f;
    public Particle particleType = Particle.FLASH;
    public Sound soundType = Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR;
    public float velocityFactor = 1.0f;

    private boolean playSound = true;

    public FireworkProjectile(ItemStack ammunitionItem) {
        super(ProjectileType.FIREWORK, ammunitionItem);
        // Defaults
    }

    public static FireworkProjectile getDefaultRocketShot(ItemStack rocketItem) {
        FireworkProjectile fireProj = new FireworkProjectile(rocketItem);
        fireProj.projectileCount = 1;
        fireProj.entityType = EntityType.FIREWORK_ROCKET;
        fireProj.particleType = Particle.WHITE_ASH;
        fireProj.soundType = Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR;
        fireProj.inaccuracy = 0.125f;
        return fireProj;
    }

    @Override
    public void Shoot(Entity player, Entity entity, Location FireLocation, Float velocity) {
        playSound = true;
        int baseDelay = 0;
        for (int i = 0; i < projectileCount; i++) {
            if (delayedFire) {
                baseDelay += delayTime;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SiegeEngines.getInstance(), () -> CreateEntity(entity, FireLocation, velocity * velocityFactor, player), baseDelay);
            } else {
                CreateEntity(entity, FireLocation, velocity * velocityFactor, player);
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
        if (arrow instanceof Firework firework) {
            if (player instanceof org.bukkit.projectiles.ProjectileSource) {
                firework.setShotAtAngle(true);
                firework.setShooter((org.bukkit.projectiles.ProjectileSource) player);
                ItemStack rocketItem = getAmmuinitionItem();
                if (rocketItem.getItemMeta() instanceof FireworkMeta) {
                    firework.setFireworkMeta((FireworkMeta) rocketItem.getItemMeta());
                }
                int fuse = firework.getTicksToDetonate();
                int itemPower = 1 + firework.getFireworkMeta().getPower();
                fuse = fuse + (5 * itemPower);
                firework.setTicksToDetonate(fuse);
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