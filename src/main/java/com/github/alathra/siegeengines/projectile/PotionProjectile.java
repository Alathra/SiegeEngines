package com.github.alathra.siegeengines.projectile;

import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.SiegeEnginesUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PotionProjectile extends SiegeEngineProjectile {
	
    public int EntityCount = 3;
    public Boolean DelayedFire = false;
    public int DelayTime = 6;
    public float Inaccuracy = 0.1f;
    public Particle ParticleType = Particle.EXPLOSION_LARGE;
    public Sound SoundType = Sound.ENTITY_GENERIC_EXPLODE;
    
    public PotionProjectile(ItemStack ammunitionItem) {
		super(ProjectileType.POTION, ammunitionItem);
	}
    
    @Override
    public void Shoot(Entity player, Entity entity, Location loc, Float velocity) {
        //TODO Auto-generated method stub
        int baseDelay = 0;
        for (int i = 0; i < EntityCount; i++) {
            if (DelayedFire) {
                baseDelay += DelayTime;
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
        if (Inaccuracy != 0f) {
            arrow.setVelocity(loc.getDirection().multiply(velocity).add(Randomise()).subtract(Randomise()));
        } else {
            arrow.setVelocity(loc.getDirection().multiply(velocity));
        }
        arrow.setMetadata("isExplosiveProj", SiegeEnginesUtil.addMetaDataValue("true"));
        Bukkit.getServer().getPluginManager().callEvent(new org.bukkit.event.entity.ProjectileLaunchEvent(arrow));
        ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 100, 3, true), true);
        itemStack.setItemMeta(potionMeta);
        ThrownPotion potion = (ThrownPotion) arrow;
        potion.setItem(itemStack);
        world.playSound(loc, this.SoundType, 20, 2);
        world.spawnParticle(this.ParticleType, loc.getX(), loc.getY(), loc.getZ(), 0);
    }

    private Vector Randomise() {
        return new Vector(SiegeEngines.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1), SiegeEngines.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1), SiegeEngines.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1));
    }

	public ProjectileType getProjectileType() {
		return projectileType;
	}
}
