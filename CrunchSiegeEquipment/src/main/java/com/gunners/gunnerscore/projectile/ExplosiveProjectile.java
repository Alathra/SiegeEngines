package com.gunners.gunnerscore.projectile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.gunners.gunnerscore.GunnersCore;
import com.gunners.gunnerscore.listeners.ClickHandler;
//import me.libraryaddict.disguise.disguisetypes.DisguiseType;
//import me.libraryaddict.disguise.disguisetypes.MiscDisguise;

public class ExplosiveProjectile implements GunnersProjectile {
	
	public String ProjectileType = "Explosive";
	public Boolean PlaceBlocks = false;
	public Material BlockToPlace = Material.COBWEB;
	public int BlocksToPlaceAmount = 3;
	public float ExplodePower = 2;
	public float Inaccuracy = 0.3f;
	public int ProjectilesCount = 1;
	public Boolean DelayedFire = false;
	public int DelayTime = 6;
	public Particle ParticleType = Particle.EXPLOSION_LARGE;
	public Sound SoundType = Sound.ENTITY_GENERIC_EXPLODE;
	private boolean PlaySound = true;
	public Boolean AlertOnLanding = false;
	@Override
	public void Shoot(Entity player, Entity entity, Location loc, Float velocity) {
		PlaySound = true;
		int baseDelay = 0;
		for (int i = 0; i < ProjectilesCount; i++) {
			if (DelayedFire) {
				baseDelay += DelayTime;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GunnersCore.plugin, () -> {
					CreateEntity(entity, loc, velocity, player);
				}, (long) baseDelay);
			}
			else {
				CreateEntity(entity,loc, velocity, player);
			}
	
			//	}
		}
	}

	private void CreateEntity(Entity entity, Location loc, Float velocity, Entity player) {
		LivingEntity living = (LivingEntity) entity;
		World world = entity.getLocation().getWorld();
		Entity tnt = world.spawnEntity(loc, EntityType.SNOWBALL);
		Snowball ball = (Snowball) tnt;
		if (player instanceof org.bukkit.projectiles.ProjectileSource)
			ball.setShooter((org.bukkit.projectiles.ProjectileSource)player);
		ClickHandler.projectiles.put(tnt.getUniqueId(), this);
		
		if (Inaccuracy != 0f) {
			tnt.setVelocity(loc.getDirection().multiply(velocity).add(Randomise())
					.subtract(Randomise()));
		}
		else {
			tnt.setVelocity(loc.getDirection().multiply(velocity));
		}
		tnt.setMetadata("isExplosiveProj",GunnersCore.addMetaDataValue("true"));
		Bukkit.getServer().getPluginManager().callEvent(new org.bukkit.event.entity.ProjectileLaunchEvent(tnt));

		if (PlaySound) {
			world.playSound(loc, this.SoundType, 20, 2);
			world.spawnParticle(this.ParticleType, loc.getX(), loc.getY(), loc.getZ(), 0);
			if (!DelayedFire) {
				PlaySound = false;
			}
		}
//		MiscDisguise miscDisguise = new MiscDisguise(DisguiseType.ARROW);
//		miscDisguise.setEntity(tnt);
//		miscDisguise.startDisguise();

	}
	
	private Vector Randomise() {
		return new Vector(GunnersCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1),GunnersCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1),GunnersCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1));
	}
}


