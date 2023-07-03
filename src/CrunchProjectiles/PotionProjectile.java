package CrunchProjectiles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import siegeCore.CrunchSiegeCore;

public class PotionProjectile implements CrunchProjectile {
	public int EntityCount = 20;
	public Boolean DelayedFire = false;
	public int DelayTime = 6;
	public float Inaccuracy = 0.4f;
	public Particle ParticleType = Particle.EXPLOSION_LARGE;
	public Sound SoundType = Sound.ENTITY_GENERIC_EXPLODE;
	
	@Override
	public void Shoot(Player player, Entity entity, double XOffset, double YOffset, Float velocity) {
		// TODO Auto-generated method stub
		int baseDelay = 0;
		for (int i = 0; i < EntityCount; i++) {
			if (DelayedFire) {
				baseDelay += DelayTime;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CrunchSiegeCore.plugin, () -> {
			
					CreateEntity(entity, XOffset, YOffset, velocity);
				}, (long) baseDelay);
			}
			else {

				CreateEntity(entity, XOffset, YOffset, velocity);
			}
	
			//	}
		}
	}
	private void CreateEntity(Entity entity,  double XOffset, double YOffset, Float velocity) {
		World world = entity.getLocation().getWorld();
		LivingEntity living = (LivingEntity) entity;
		Location loc =  living.getEyeLocation();
		Vector direction = entity.getLocation().getDirection().multiply(XOffset);
		loc.add(direction);

		Entity arrow = world.spawnEntity(loc, EntityType.SPLASH_POTION);
		if (Inaccuracy != 0f) {
			arrow.setVelocity(loc.getDirection().multiply(velocity).add(Randomise())
					.subtract(Randomise()));
		}
		else {
			arrow.setVelocity(loc.getDirection().multiply(velocity));
		}
		ItemStack itemStack = new ItemStack(Material.LINGERING_POTION);
		PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
		potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 100, 0, true), true);
		itemStack.setItemMeta(potionMeta);
		ThrownPotion  potion = (ThrownPotion) arrow;
		potion.setItem(itemStack);
		//				MiscDisguise miscDisguise = new MiscDisguise(DisguiseType.DROPPED_ITEM, Material.IRON_NUGGET);
		//				DisguiseAPI.disguiseEntity(arrow, miscDisguise);
		world.playSound(loc, this.SoundType, 20, 2);
		world.spawnParticle(this.ParticleType, loc.getX(), loc.getY(), loc.getZ(), 0);
	}
	
	private Vector Randomise() {
		return new Vector(CrunchSiegeCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1),CrunchSiegeCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1),CrunchSiegeCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1));
	}
}
