package com.github.alathra.siegeengines.listeners;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.SiegeEnginesLogger;
import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.Util.GeneralUtil;
import com.github.alathra.siegeengines.Util.SiegeEnginesUtil;
import com.github.alathra.siegeengines.SiegeEngine;
import com.github.alathra.siegeengines.projectile.ExplosiveProjectile;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ClickHandler implements Listener {

	public float MinDelay = 5;
	public static final ArrayList<ItemStack> items = new ArrayList<>();

	public static HashMap<UUID, ExplosiveProjectile> projectiles = new HashMap<UUID, ExplosiveProjectile>();
	public static EnumSet<Material> fluidMaterials = EnumSet.of(Material.WATER, Material.LAVA, Material.BUBBLE_COLUMN,
			Material.SEAGRASS, Material.TALL_SEAGRASS, Material.KELP, Material.KELP_PLANT, Material.SEA_PICKLE);
	NamespacedKey key = new NamespacedKey(SiegeEngines.getInstance(), "siege_engines");

	@EventHandler
	public void onSiegeEngineExplode(EntityExplodeEvent e) {
		
		// called when a siege engine blows up??
		
		// TODO: VERIFY THAT ENTITY IS A SIEGE ENGINE
		
		for (Entity entity : e.getEntity().getNearbyEntities(e.getYield() + 1, e.getYield() + 1, e.getYield() + 1)) {
			if (entity instanceof ArmorStand) {
				ArmorStand stand = (ArmorStand) entity;
				stand.eject();
				stand.remove();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onSiegeEngineHitByProjectile(ProjectileHitEvent event) {
		if ((event.getEntity() instanceof Projectile)) {
			for (Entity entity : event.getEntity().getNearbyEntities(2, 2, 2)) {
				if (entity instanceof ArmorStand) {
					ArmorStand stand = (ArmorStand) entity;
					if (event.isCancelled()) return;
					if (SiegeEnginesUtil.isSiegeEngine(stand,false)) {
						event.setCancelled(true);
						SiegeEnginesLogger.debug("ARROW DAMAGE CANCELLED? : "+Config.arrowDamageToggle);
						if (Config.arrowDamageToggle) {
							continue;
						}
						SiegeEnginesLogger.debug("HEALTH BEOFRE SHOT : "+stand.getHealth());
						if (stand.getHealth()-2 > 0) {
							stand.setHealth(stand.getHealth()-2);
						} else {
							EntityDeathEvent death = new EntityDeathEvent(stand,items,0);
							Bukkit.getServer().getPluginManager().callEvent(death);
							if (!death.isCancelled()) stand.setHealth(0.0f);
						}
						SiegeEnginesLogger.debug("HEALTH AFTER SHOT : "+stand.getHealth());
						if (stand.isDead()) {
							PlayerHandler.siegeEngineEntityDied(stand);
						}
					}
				}
			}
		}
		if ((event.getEntity() instanceof Projectile)
				&& projectiles.containsKey(event.getEntity().getUniqueId())) {
			ExplosiveProjectile proj = projectiles.get(event.getEntity().getUniqueId());
			Entity snowball = event.getEntity();
			org.bukkit.entity.Projectile ball = (org.bukkit.entity.Projectile) snowball;
			Entity player = (Entity) ball.getShooter();
			if (player instanceof Player) {
				player.sendMessage("§eDistance to impact: "
						+ String.format("%.2f", player.getLocation().distance(ball.getLocation())));
			}
			Location loc = snowball.getLocation();
			Entity tnt = event.getEntity().getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);

			projectiles.remove(event.getEntity().getUniqueId());

			if (proj.placeBlocks) {
				TNTPrimed tntEnt = (TNTPrimed) tnt;
				tntEnt.setYield(0);
				tntEnt.setFuseTicks(0);
				if (event.getHitBlock() != null) {
					List<Block> Blocks = GeneralUtil.getSphere(event.getHitBlock().getLocation(), (int) proj.explodePower);
					for (int i = 0; i < proj.blocksToPlaceAmount; i++) {
						Block replace = (Block) GeneralUtil.getRandomElement(Blocks);
						replace.setType(proj.blockToPlace);
					}
				}
			} else {
				TNTPrimed tntEnt = (TNTPrimed) tnt;
				tntEnt.setYield(proj.explodePower);
				tntEnt.setFuseTicks(0);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onSiegeEnginePlace(org.bukkit.event.block.BlockPlaceEvent event) {
		Player thePlayer = event.getPlayer();
		Material replaced = event.getBlockReplacedState().getType();
		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.CARVED_PUMPKIN) {
			ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
			if (item.getItemMeta() != null && item.getItemMeta().hasCustomModelData()) {
				int customModel = item.getItemMeta().getCustomModelData();
				SiegeEngine siegeEngine = null;
				// Search for match in custom model data value in defined siege engines
				for (SiegeEngine entry : SiegeEngines.definedSiegeEngines.values()) {
					if (entry.getCustomModelID() == customModel) {
						try {
							siegeEngine = entry.clone();
						} catch (CloneNotSupportedException e) {
							break;
						}
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
					if (Config.disabledWorlds.contains(thePlayer.getWorld())) {
						thePlayer.sendMessage("§eSiege Engines cannot be placed in this World.");
						event.setCancelled(true);
					}
					if (fluidMaterials.contains(replaced)) {
						thePlayer.sendMessage("§eSiege Engines cannot be placed in Fluid Blocks.");
						event.setCancelled(true);
					}
					if (event.isCancelled()) {
						return;
					}
					if (siegeEngine.place(thePlayer, event.getBlockAgainst().getLocation())) {
						item.setAmount(item.getAmount() - 1);
						thePlayer.getInventory().setItemInMainHand(item);
						thePlayer.sendMessage("§eSiege Engine placed!");
					} else {
						thePlayer.sendMessage(
								"§eSiege Engine cannot be placed within a "+ Config.placementDensity+ " Block-Radius of other Siege Engines.");
					}
					event.setCancelled(true);
				}
			}
		} else if (event.getPlayer().getInventory().getItemInOffHand().getType() == Material.CARVED_PUMPKIN) {
			ItemStack item = event.getPlayer().getInventory().getItemInOffHand();
			if (item.getItemMeta() != null && item.getItemMeta().hasCustomModelData()) {
				int customModel = item.getItemMeta().getCustomModelData();
				SiegeEngine siegeEngine = null;
				// Search for match in custom model data value in defined siege engines
				for (SiegeEngine entry : SiegeEngines.definedSiegeEngines.values()) {
					if (entry.getCustomModelID() == customModel) {
						try {
							siegeEngine = entry.clone();
						} catch (CloneNotSupportedException e) {
							break;
						}
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
					if (Config.disabledWorlds.contains(thePlayer.getWorld())) {
						thePlayer.sendMessage("§eSiege Engines cannot be placed in this World.");
						event.setCancelled(true);
					}
					if (fluidMaterials.contains(replaced)) {
						thePlayer.sendMessage("§eSiege Engines cannot be placed in Fluid Blocks.");
						event.setCancelled(true);
					}
					if (event.isCancelled()) {
						return;
					}
					if (siegeEngine.place(thePlayer, event.getBlockAgainst().getLocation())) {
						item.setAmount(item.getAmount() - 1);
						thePlayer.getInventory().setItemInOffHand(item);
						thePlayer.sendMessage("§eSiege Engine placed!");
					} else {
						thePlayer.sendMessage(
								"§eSiege Engine cannot be placed within a "+Config.placementDensity+" Block-Radius of other Siege Engines.");
					}
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onSiegeEngineFire(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack ItemInHand = event.getPlayer().getInventory().getItemInMainHand();
		if (ItemInHand == null) {
			return;
		}

		if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			if (ItemInHand.getType() != Config.controlItem) {
				return;
			}
			// If player is not sneaking, fire SiegeEngine
			if (!player.isSneaking()) {
				List<Entity> siegeEngineEntities = SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId());
				if (siegeEngineEntities == null) {
					return;
				}
				for (Entity siegeEngineEntity : siegeEngineEntities) {
					if (SiegeEngines.activeSiegeEngines.containsKey(siegeEngineEntity.getUniqueId())) {
						SiegeEngine siegeEngine = SiegeEngines.activeSiegeEngines.get(siegeEngineEntity.getUniqueId());
						if (siegeEngine != null && siegeEngine.getEnabled() && !(siegeEngineEntity.isDead())
								&& siegeEngine.isLoaded()) {
							if (siegeEngine.isSetModelNumberWhenFullyLoaded() && siegeEngine.canLoadFuel()) {
								player.sendMessage("§eFailed to fire. This siege engine needs to be fully loaded!");
							} else {
								siegeEngine.Fire(player, 10f, 1);
							}
						}
					}
				}
				return;
			}
		}
		
		// Attempt auto-reload if enabled in config
		if (Config.autoReload) {
			SiegeEnginesUtil.autoReload(player);
		}
		
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onSiegeEngineDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand)event.getEntity();
			if (SiegeEnginesUtil.isSiegeEngine(stand,false)) return;
			if (event.isCancelled()) return;
			if (event.getDamager() instanceof Player) {
				PlayerHandler.releasePlayerSiegeEngine((Player)(event.getDamager()),event.getEntity());
			}
			SiegeEnginesLogger.debug("HEALTH BEOFRE HIT : "+stand.getHealth());
			if (stand.getHealth()-2 > 0) {
				stand.setHealth(stand.getHealth()-2);
			} else {
				EntityDeathEvent death = new EntityDeathEvent(stand,items,0);
				Bukkit.getServer().getPluginManager().callEvent(death);
				if (death.isCancelled()) return;
				stand.setHealth(0.0f);
				PlayerHandler.siegeEngineEntityDied(event.getEntity());
			}
			SiegeEnginesLogger.debug("HEALTH AFTER HIT : "+stand.getHealth());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onSiegeEngineDeathEvent(EntityDeathEvent event) {
		boolean removeStands = false;
		List<ItemStack> items = event.getDrops();
		if (event.getEntity() instanceof ArmorStand) {
			if (event.getEntity().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
				Entity base = Bukkit.getEntity(UUID.fromString(
						event.getEntity().getPersistentDataContainer().get(key, PersistentDataType.STRING)));
				base.remove();
			}
			if (SiegeEngines.activeSiegeEngines.containsKey(event.getEntity().getUniqueId())) {
				removeStands = true;
			} else {
				for (ItemStack i : items) {
					if (i.getType() == Material.CARVED_PUMPKIN && i.hasItemMeta()
							&& i.getItemMeta().hasCustomModelData()) {
						// If siege engine is default model number
						if (SiegeEngines.definedSiegeEngines.containsKey(i.getItemMeta().getCustomModelData())) {
							removeStands = true;
							break;
						}
						// If siege engine is some other model number because it was broken in the middle of firing
						for (SiegeEngine siegeEngine : SiegeEngines.definedSiegeEngines.values()) {
							if (siegeEngine.getFiringModelNumbers().contains(i.getItemMeta().getCustomModelData())) {
								removeStands = true;
								break;
							}
						}
					}
				}
			}
			if (removeStands) {
				for (ItemStack i : items) {
					if (i.getType() == Material.ARMOR_STAND) {
						PlayerHandler.siegeEngineEntityDied(event.getEntity(),false);
						SiegeEngines.activeSiegeEngines.remove(event.getEntity().getUniqueId());
						i.setAmount(0);
						return;
					}
				}
			}
		}
	}

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
						if (fluidMaterials.contains(entity.getLocation().getBlock().getType())) {
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