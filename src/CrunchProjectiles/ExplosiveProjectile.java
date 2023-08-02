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
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import siegeCore.ClickHandler;
import siegeCore.CrunchSiegeCore;

public class ExplosiveProjectile implements CrunchProjectile {
	
	public String ProjectileType = "Explosive";
	public Boolean PlaceBlocks = false;
	public Material BlockToPlace = Material.COBWEB;
	public int BlocksToPlaceAmount = 3;
	public int ExplodePower = 2;
	public float Inaccuracy = 0.3f;
	public int ProjectilesCount = 1;
	public Boolean DelayedFire = false;
	public int DelayTime = 6;
	public Particle ParticleType = Particle.EXPLOSION_LARGE;
	public Sound SoundType = Sound.ENTITY_GENERIC_EXPLODE;
	private boolean PlaySound = true;
	public Boolean AlertOnLanding = false;
	@Override
	public void Shoot(Player player, Entity entity, Location loc, Float velocity) {
		PlaySound = true;
		int baseDelay = 0;
		for (int i = 0; i < ProjectilesCount; i++) {
			if (DelayedFire) {
				baseDelay += DelayTime;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CrunchSiegeCore.plugin, () -> {
					CreateEntity(entity, loc, velocity, player);
				}, (long) baseDelay);
			}
			else {
			
				CreateEntity(entity,loc, velocity, player);
			}
	
			//	}
		}
	}

	private void CreateEntity(Entity entity, Location loc, Float velocity, Player player) {
		LivingEntity living = (LivingEntity) entity;
		World world = entity.getLocation().getWorld();
		Entity tnt = world.spawnEntity(loc, EntityType.SNOWBALL);
		Snowball ball = (Snowball) tnt;
		
		ball.setShooter(player);
		ClickHandler.projectiles.put(tnt.getUniqueId(), this);
		
		if (Inaccuracy != 0f) {
			tnt.setVelocity(loc.getDirection().multiply(velocity).add(Randomise())
					.subtract(Randomise()));
		}
		else {
			tnt.setVelocity(loc.getDirection().multiply(velocity));
		}

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
		return new Vector(CrunchSiegeCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1),CrunchSiegeCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1),CrunchSiegeCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1));
	}
}


