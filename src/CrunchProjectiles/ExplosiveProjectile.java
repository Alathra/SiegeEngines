package CrunchProjectiles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import siegeCore.ClickHandler;
import siegeCore.CrunchSiegeCore;

public class ExplosiveProjectile implements CrunchProjectile {

	public int Radius = 2;
	public float Inaccuracy = 3.0f;
	public int ProjectilesCount = 1;
	@Override
	public void Shoot(Player player, Entity entity, Location loc, Float velocity) {
		for (int i = 0; i < ProjectilesCount; i++) {
			if (Inaccuracy > 0) {
				float randomVar = CrunchSiegeCore.random.nextFloat() * (Inaccuracy - (Inaccuracy * -1)) + (Inaccuracy * -1);
				loc.setYaw(loc.getYaw() + randomVar);
			}
			
			World world = entity.getLocation().getWorld();
			Entity tnt = world.spawnEntity(loc, EntityType.SNOWBALL);
			ClickHandler.projectiles.put(tnt.getUniqueId(), this);
			tnt.setVelocity(loc.getDirection().multiply(velocity));
			world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 20, 2);
			world.spawnParticle(Particle.EXPLOSION_LARGE, loc.getX(), loc.getY(), loc.getZ(), 0);
		}
	}
}


