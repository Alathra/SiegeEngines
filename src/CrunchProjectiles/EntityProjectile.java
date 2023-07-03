package CrunchProjectiles;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import siegeCore.CrunchSiegeCore;

public class EntityProjectile implements CrunchProjectile{

	public int EntityCount = 20;
	public int ArrowOnlyDamage = 6;
	public EntityType EntityTyp = EntityType.ARROW;
	@Override
	public void Shoot(Player player, Entity entity, Location loc, Float velocity) {

		for (int i = 0; i < EntityCount; i++) {
			World world = entity.getLocation().getWorld();

			Entity arrow = world.spawnEntity(loc, EntityTyp);
			arrow.setVelocity(loc.getDirection().multiply(velocity).add(new Vector(CrunchSiegeCore.random.nextDouble(), CrunchSiegeCore.random.nextDouble(), CrunchSiegeCore.random.nextDouble())
					.subtract(new Vector(CrunchSiegeCore.random.nextDouble(), CrunchSiegeCore.random.nextDouble(), CrunchSiegeCore.random.nextDouble()))));
			if (arrow instanceof Arrow) {
				Arrow arr = (Arrow) arrow;
				arr.setDamage(8);
			}
			//				MiscDisguise miscDisguise = new MiscDisguise(DisguiseType.DROPPED_ITEM, Material.IRON_NUGGET);
			//				DisguiseAPI.disguiseEntity(arrow, miscDisguise);
			world.playSound(entity.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 20, 2);
			//	}
		}
	}
}

