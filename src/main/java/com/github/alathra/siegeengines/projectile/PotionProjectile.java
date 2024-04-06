package com.github.alathra.siegeengines.projectile;

import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.Util.SiegeEnginesUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.Vector;

public class PotionProjectile extends SiegeEngineProjectile {
	
    public int projectileCount = 3;
    public Boolean delayedFire = false;
    public int delayTime = 6;
    public float inaccuracy = 0.1f;
    public Particle particleType = Particle.EXPLOSION_LARGE;
    public Sound soundType = Sound.ENTITY_GENERIC_EXPLODE;
    public float velocityFactor = 1.0f;

    public PotionProjectile(ItemStack ammunitionItem) {
		super(ProjectileType.POTION, ammunitionItem);
	}

    @Override
    public void Shoot(Entity player, Entity entity, Location loc, Float velocity) {
        //TODO Auto-generated method stub
        int baseDelay = 0;
        for (int i = 0; i < projectileCount; i++) {
            if (delayedFire) {
                baseDelay += delayTime;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SiegeEngines.getInstance(), () -> {

                    CreateEntity(entity, loc, velocity);
                }, (long) baseDelay);
            } else {
                CreateEntity(entity, loc, velocity);
            }
        }
    }

    private void CreateEntity(Entity entity, Location loc, Float velocity) {
        World world = entity.getLocation().getWorld();

        Entity arrow = world.spawnEntity(loc, EntityType.SPLASH_POTION);
        if (inaccuracy != 0f) {
            arrow.setVelocity(loc.getDirection().multiply(velocity).add(Randomise()).subtract(Randomise()));
        } else {
            arrow.setVelocity(loc.getDirection().multiply(velocity));
        }
        arrow.setMetadata("isPotionProj", SiegeEnginesUtil.addMetaDataValue("true"));
        Bukkit.getServer().getPluginManager().callEvent(new org.bukkit.event.entity.ProjectileLaunchEvent(arrow));
        ItemStack itemStack = this.getAmmuinitionItem();
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        System.out.println(potionMeta.toString());
        System.out.println(itemStack.toString());
        potionMeta.getBasePotionData();
        itemStack.setItemMeta(potionMeta);
        ThrownPotion potion = (ThrownPotion) arrow;
        potion.setItem(itemStack);
        world.playSound(loc, this.soundType, 20, 2);
        world.spawnParticle(this.particleType, loc.getX(), loc.getY(), loc.getZ(), 0);
    }

    private Vector Randomise() {
        return new Vector(SiegeEngines.random.nextFloat() * (inaccuracy - (inaccuracy * -1)) + (inaccuracy * -1), SiegeEngines.random.nextFloat() * (inaccuracy - (inaccuracy * -1)) + (inaccuracy * -1), SiegeEngines.random.nextFloat() * (inaccuracy - (inaccuracy * -1)) + (inaccuracy * -1));
    }

	public ProjectileType getProjectileType() {
		return projectileType;
	}
}
