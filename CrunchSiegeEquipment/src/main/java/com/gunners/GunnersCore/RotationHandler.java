package com.gunners.GunnersCore;

import java.awt.Color;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class RotationHandler implements Listener {

	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		NamespacedKey key = new NamespacedKey(GunnersCore.plugin, "cannons");	
		if (GunnersCore.TrackedStands.containsKey(player.getUniqueId())) {
			ItemStack itemInHand = player.getInventory().getItemInMainHand();
			if (itemInHand != null) {
				if (itemInHand.getType() != Material.COMPASS) {
					//	TrackedStands.remove(player.getUniqueId());

					return;
				}
				for (Entity ent : GunnersCore.TrackedStands.get(player.getUniqueId())) {
					if (ent != null) {
						GunnerEquipment equipment = GunnersCore.equipment.get(ent.getUniqueId());
						if (ent.isDead()) {
							continue;
						}
						double distance = player.getLocation().distance(ent.getLocation());
						if (distance <= 250) {
							//	player.sendMessage("§egot id");
							LivingEntity living = (LivingEntity) ent;
							Location loc = ent.getLocation();
							
							if (equipment.RotateSideways) {
								Location direction = player.getLocation().add(player.getLocation().getDirection().multiply(50));

								Vector dirBetweenLocations = direction.toVector().subtract(loc.toVector());
								if (ent.getPersistentDataContainer().has(key,  PersistentDataType.STRING)) {
									Entity base = Bukkit.getEntity(UUID.fromString(ent.getPersistentDataContainer().get(key, PersistentDataType.STRING)));
									Location baseloc = base.getLocation();
									baseloc.setDirection(dirBetweenLocations);
									base.teleport(baseloc);
								}
								loc.setDirection(dirBetweenLocations);
							}
							//loc.setYaw(player.getLocation().getYaw());
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
								if (equipment.RotateStandHead) {
								stand.setHeadPose(new EulerAngle(loc.getDirection().getY()*(-1),0,0));
								}
							}
						
							
							living.teleport(loc);
						    equipment.ShowFireLocation(player);     
						}
					}	
				}
			}
		}
	}
	
	 Vector getDirectionBetweenLocations(Location Start, Location End) {
	        Vector from = Start.toVector();
	        Vector to = End.toVector();
	        return to.subtract(from);
	    }
}
