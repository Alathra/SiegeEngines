package com.gunners.gunnerscore.projectile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.gunners.gunnerscore.GunnersCore;


public class EntityProjectile implements GunnersProjectile{

	public String ProjectileType = "Entity";
	public int EntityCount = 20;
	public int ArrowOnlyDamage = 6;
	public Boolean DelayedFire = false;
	public int DelayTime = 6;
	public EntityType EntityTyp = EntityType.ARROW;
	public float Inaccuracy = 0.2f;
	public Particle ParticleType = Particle.EXPLOSION_LARGE;
	public Sound SoundType = Sound.ENTITY_GENERIC_EXPLODE;
	@Override
	public void Shoot(Entity player, Entity entity, Location FireLocation, Float velocity) {
		PlaySound = true;
		int baseDelay = 0;
		for (int i = 0; i < EntityCount; i++) {
			if (DelayedFire) {
				baseDelay += DelayTime;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GunnersCore.plugin, () -> {
					
					CreateEntity(entity, FireLocation, velocity, player);
				}, (long) baseDelay);
			}
			else {

				CreateEntity(entity, FireLocation, velocity, player);
			}
	
			//	}
		}
	}
	private boolean PlaySound = true;
	private void CreateEntity(Entity entity,  Location loc, Float velocity, Entity player) {
		World world = entity.getLocation().getWorld();
		LivingEntity living = (LivingEntity) entity;

		Entity arrow = world.spawnEntity(loc, EntityTyp);
		if (Inaccuracy != 0f) {
			arrow.setVelocity(loc.getDirection().multiply(velocity).add(Randomise())
					.subtract(Randomise()));
		}
		else {
			arrow.setVelocity(loc.getDirection().multiply(velocity));
		}
	
		if (arrow instanceof Arrow) {
			Arrow arr = (Arrow) arrow;
			arr.setDamage(8);
			if (player instanceof org.bukkit.projectiles.ProjectileSource)
				arr.setShooter((org.bukkit.projectiles.ProjectileSource)player);
			//arr.setBasePotionData(arg0);
		}
		//				MiscDisguise miscDisguise = new MiscDisguise(DisguiseType.DROPPED_ITEM, Material.IRON_NUGGET);
		//				DisguiseAPI.disguiseEntity(arrow, miscDisguise);
		if (PlaySound) {
			world.playSound(loc, this.SoundType, 20, 2);
			world.spawnParticle(this.ParticleType, loc.getX(), loc.getY(), loc.getZ(), 0);
			if (!DelayedFire) {
				PlaySound = false;
			}
		}

	}
	
	private Vector Randomise() {
		return new Vector(GunnersCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1),GunnersCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1),GunnersCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1));
	}
}

