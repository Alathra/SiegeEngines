package siegeCore;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class RotationHandler implements Listener {
	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (CrunchSiegeCore.TrackedStands.containsKey(player.getUniqueId())) {
			ItemStack itemInHand = player.getInventory().getItemInMainHand();
			if (itemInHand != null) {
				if (itemInHand.getType() != Material.CLOCK) {
					//	TrackedStands.remove(player.getUniqueId());

					return;
				}
				for (Entity ent : CrunchSiegeCore.TrackedStands.get(player.getUniqueId())) {
					if (ent != null) {
						SiegeEquipment equipment = CrunchSiegeCore.equipment.get(ent.getUniqueId());
						
						double distance = player.getLocation().distance(ent.getLocation());
						if (distance <= 250) {
							//	player.sendMessage("got id");
							LivingEntity living = (LivingEntity) ent;
							Location loc = ent.getLocation();
							
							if (equipment.RotateSideways) {
								
					
							Location direction = player.getLocation().add(player.getLocation().getDirection().multiply(50));

							Vector dirBetweenLocations = direction.toVector().subtract(loc.toVector());

							loc.setDirection(dirBetweenLocations);
							}
						//	loc.setYaw(player.getLocation().getYaw());
							//loc.setPitch(player.getLocation().getPitch());
							
							
							ArmorStand stand = (ArmorStand) living;
							
							if (equipment.RotateUpDown) {
								loc.setPitch(player.getLocation().getPitch());
								if (loc.getPitch()  < -85) {
									loc.setPitch(-85);
								}
								if (loc.getPitch() > 85) {
									loc.setPitch(85);
								}
								stand.setHeadPose(new EulerAngle(loc.getDirection().getY()*(-1),0,0));
						
							}
						
							
							living.teleport(loc);
						}
					}	
				}
			}
		}
	}
}
