package com.github.alathra.siegeengines.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.github.alathra.siegeengines.SiegeEngine;
import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.Util.SiegeEnginesUtil;
import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.data.SiegeEnginesData;

public class SiegeEngineInteractListener implements Listener {
	@EventHandler(priority = EventPriority.LOW)
	public void onSiegeEngineClick(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getInventory().getItemInMainHand();
		Entity entity = event.getRightClicked();
		if (entity == null) {
			return;
		}
		if (entity.getType() == EntityType.ARMOR_STAND) {
			if ((itemInHand.getType() == Material.AIR || itemInHand == null))  {
				if (player.isSneaking()) {
					if (!(SiegeEngines.activeSiegeEngines.containsKey(entity.getUniqueId()))) {
						return;
					}
					PlayerHandler.siegeEngineEntityDied(entity,true);
					if (SiegeEnginesUtil.isSiegeEngine(entity, false)) {
						PlayerHandler.releasePlayerSiegeEngine(player,entity);
					}
					return;
				}
			}
			if (!(player.isSneaking()) && itemInHand.getType() == Config.controlItem) {
				if (SiegeEnginesUtil.isSiegeEngine(entity, true)) SiegeEnginesUtil.takeControl(player, entity);
			}
			if (SiegeEngines.activeSiegeEngines.containsKey(entity.getUniqueId())) {
				SiegeEngine siegeEngine = SiegeEngines.activeSiegeEngines.get(entity.getUniqueId());
				event.setCancelled(true);
				ItemStack stack = siegeEngine.getFuelItem();
				if (itemInHand.getType() == stack.getType()) {
					if (siegeEngine.canLoadFuel()) {
						if (itemInHand.isSimilar(stack)) {
							siegeEngine.getAmmoHolder().setLoadedFuel(siegeEngine.getAmmoHolder().getLoadedFuel() + 1);
							stack.setAmount(1);
							player.getInventory().removeItem(stack);
							SiegeEnginesUtil.sendSiegeEngineHelpMSG(player, siegeEngine);
							// If fully loaded && requires change in animation when loaded (i.e. ballista)
							if (!siegeEngine.canLoadFuel() && siegeEngine.isSetModelNumberWhenFullyLoaded()) {
								SiegeEnginesUtil.UpdateEntityIdModel(siegeEngine.getEntity(), siegeEngine.getPreLoadModelNumber(), siegeEngine.getWorldName());
							}
						}
					}
				}
				if (SiegeEnginesUtil.pulledHeldAmmoFromPlayer(player, siegeEngine)) {
					player.sendMessage("§eAdded ammunition to this Siege Engine.");
					// If requires change in animation when loaded with a projectile (i.e. ballista)
					if (siegeEngine.isSetModelNumberWhenFullyLoaded()) {
						SiegeEnginesUtil.UpdateEntityIdModel(siegeEngine.getEntity(), siegeEngine.getPreFireModelNumber(), siegeEngine.getWorldName());
					}
					return;
				}
				if (itemInHand == null || itemInHand.getType() == Material.AIR
						|| itemInHand.getType() == Config.controlItem) {
					if (!SiegeEnginesUtil.pulledPropellantFromContainer(siegeEngine.getEntity().getLocation(), siegeEngine)) {
						SiegeEnginesUtil.sendSiegeEngineHelpMSG(player, siegeEngine);
						return;
					}
					if (!SiegeEnginesUtil.pulledPropellantFromContainer(
							siegeEngine.getEntity().getLocation().getBlock().getRelative(0, -1, 0).getLocation(), siegeEngine)) {
						SiegeEnginesUtil.sendSiegeEngineHelpMSG(player, siegeEngine);
						return;
					}
					if (!SiegeEnginesUtil.pulledPropellantFromContainer(
							siegeEngine.getEntity().getLocation().getBlock().getRelative(0, 1, 0).getLocation(), siegeEngine)) {
						SiegeEnginesUtil.sendSiegeEngineHelpMSG(player, siegeEngine);
						return;
					}
					if (SiegeEnginesUtil.pulledAmmoFromContainer(siegeEngine.getEntity().getLocation(), siegeEngine)) {
						player.sendMessage("§eAdded ammunition to this Siege Engine.");
						return;
					}
					if (SiegeEnginesUtil.pulledAmmoFromContainer(
							siegeEngine.getEntity().getLocation().getBlock().getRelative(0, -1, 0).getLocation(), siegeEngine)) {
						player.sendMessage("§eAdded ammunition to this Siege Engine.");
						return;
					}
					if (SiegeEnginesUtil.pulledAmmoFromContainer(
							siegeEngine.getEntity().getLocation().getBlock().getRelative(0, 1, 0).getLocation(), siegeEngine)) {
						player.sendMessage("§eAdded ammunition to this Siege Engine.");
						return;
					}
				}
			}
		}
		if (entity.getType() == EntityType.RAVAGER || entity.getType() == EntityType.HORSE || entity.getType() == EntityType.DONKEY) {
			if (player.getInventory().getItemInMainHand().getType() == Material.CARVED_PUMPKIN) {
				final ItemStack item = player.getInventory().getItemInMainHand();
				if (item.getItemMeta() != null && item.getItemMeta().hasCustomModelData()) {
					int customModel = item.getItemMeta().getCustomModelData();
					SiegeEngine siegeEngine = null;
					// Search for match in custom model data value in defined siege engines
					for (SiegeEngine entry : SiegeEngines.definedSiegeEngines.values()) {
						if (entry.getCustomModelID() == customModel) {
							try {
								siegeEngine = entry.clone();
							} catch (CloneNotSupportedException e) {
							}
							break;
						} else {
							// if siege engine was broken during one of its firing stages
							if (entry.getFiringModelNumbers().contains(customModel)) {
								try {
									siegeEngine = entry.clone();
								} catch (CloneNotSupportedException e) {
									break;
								}
							}
						}
					}
					// If SiegeEngine found, place it
					if (siegeEngine != null) {
						if (!siegeEngine.isMountable()) {
							player.sendMessage("§eThis type of Siege Engine cannot be mounted to mobs.");
							event.setCancelled(true);
							return;
						}
						if (Config.disabledWorlds.contains(entity.getWorld())) {
							player.sendMessage("§eSiege Engines cannot be placed in this World.");
							event.setCancelled(true);
						}
						if (SiegeEnginesData.fluidMaterials.contains(entity.getLocation().getBlock().getType())) {
							player.sendMessage("§eSiege Engines cannot be placed in Fluid Blocks.");
							event.setCancelled(true);
						}
						if (event.isCancelled()) {
							return;
						}
						if (siegeEngine.place(player, entity.getLocation(),entity)) {
							// If player is in creative mode, don't remove the item from their inventory
							if (player.getGameMode() != GameMode.CREATIVE) {
								item.setAmount(item.getAmount() - 1);
							}
							player.getInventory().setItemInMainHand(item);
							player.sendMessage("§eSiege Engine mounted to the "+entity.getType().toString().toLowerCase()+"!");
						} else {
							player.sendMessage(
									"§eSiege Engine cannot be placed within a "+Config.placementDensity+" Block-Radius of other Siege Engines.");
						}
						event.setCancelled(true);
					}
				}
			}
			return;
		}
	}
}
