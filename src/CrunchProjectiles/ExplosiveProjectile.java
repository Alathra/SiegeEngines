package CrunchProjectiles;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import siegeCore.ClickHandler;
import siegeCore.CrunchSiegeCore;

public class ExplosiveProjectile implements CrunchProjectile {

	public int ExplodePower = 2;
	public float Inaccuracy = 0.4f;
	public int ProjectilesCount = 1;
	public Boolean DelayedFire = false;
	public int DelayTime = 6;
	public Particle ParticleType = Particle.EXPLOSION_LARGE;
	public Sound SoundType = Sound.ENTITY_GENERIC_EXPLODE;
	@Override
	public void Shoot(Player player, Entity entity, double XOffset, double YOffset, Float velocity) {
		int baseDelay = 0;
		for (int i = 0; i < ProjectilesCount; i++) {
			if (DelayedFire) {
				baseDelay += DelayTime;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CrunchSiegeCore.plugin, () -> {
					CreateEntity(entity,  XOffset, YOffset, velocity);
				}, (long) baseDelay);
			}
			else {
			
				CreateEntity(entity, XOffset, YOffset, velocity);
			}
	
			//	}
		}
	}

	private void CreateEntity(Entity entity, double XOffset, double YOffset, Float velocity) {
		LivingEntity living = (LivingEntity) entity;
		Location loc =  living.getEyeLocation();
		Vector direction = entity.getLocation().getDirection().multiply(XOffset);
		loc.add(direction);
		World world = entity.getLocation().getWorld();
		Entity tnt = world.spawnEntity(loc, EntityType.SNOWBALL);
		ClickHandler.projectiles.put(tnt.getUniqueId(), this);
		if (Inaccuracy != 0f) {
			tnt.setVelocity(loc.getDirection().multiply(velocity).add(Randomise())
					.subtract(Randomise()));
		}
		else {
			tnt.setVelocity(loc.getDirection().multiply(velocity));
		}

		world.playSound(loc, this.SoundType, 20, 2);
		world.spawnParticle(this.ParticleType, loc.getX(), loc.getY(), loc.getZ(), 0);
	}
	
	private Vector Randomise() {
		return new Vector(CrunchSiegeCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1),CrunchSiegeCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1),CrunchSiegeCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1));
	}
}


