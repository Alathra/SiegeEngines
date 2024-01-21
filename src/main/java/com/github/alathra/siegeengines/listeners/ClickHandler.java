package com.github.alathra.siegeengines.listeners;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.SiegeEngineAmmoHolder;
import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.SiegeEnginesUtil;

import com.github.alathra.siegeengines.SiegeEngine;
import com.github.alathra.siegeengines.projectile.ExplosiveProjectile;
import com.github.alathra.siegeengines.projectile.FireworkProjectile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;

public class ClickHandler implements Listener {

	public float MinDelay = 5;

	public static HashMap<UUID, ExplosiveProjectile> projectiles = new HashMap<UUID, ExplosiveProjectile>();
	public static EnumSet<Material> fluidMaterials = EnumSet.of(Material.WATER, Material.LAVA, Material.BUBBLE_COLUMN,
			Material.SEAGRASS, Material.TALL_SEAGRASS, Material.KELP, Material.KELP_PLANT, Material.SEA_PICKLE);

	@EventHandler
	public void onExplode(EntityExplodeEvent e) {
		for (Entity entity : e.getEntity().getNearbyEntities(e.getYield() + 1, e.getYield() + 1, e.getYield() + 1)) {
			if (entity instanceof ArmorStand) {
				ArmorStand stand = (ArmorStand) entity;
				stand.eject();
				stand.remove();
			}
		}
	}

	@EventHandler
	public void onHit(ProjectileHitEvent event) {
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
					List<Block> Blocks = sphere(event.getHitBlock().getLocation(), (int) proj.explodePower);
					for (int i = 0; i < proj.blocksToPlaceAmount; i++) {
						Block replace = getRandomElement(Blocks);
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

	public ArrayList<Block> sphere(final Location center, final int radius) {
		ArrayList<Block> sphere = new ArrayList<Block>();
		for (int Y = -radius; Y < radius; Y++) {
			for (int X = -radius; X < radius; X++) {
				for (int Z = -radius; Z < radius; Z++) {
					if (Math.sqrt((X * X) + (Y * Y) + (Z * Z)) <= radius) {
						final Block block = center.getWorld().getBlockAt(X + center.getBlockX(), Y + center.getBlockY(),
								Z + center.getBlockZ());
						if (block.getType() == Material.AIR) {
							sphere.add(block);
						}
					}
				}
			}
		}
		return sphere;
	}

	public Block getRandomElement(List<Block> list) {
		Random rand = new Random();
		return list.get(rand.nextInt(list.size()));
	}

	// On SiegeEngine place
	@EventHandler(priority = EventPriority.HIGH)
	public void BlockPlaceEvent(org.bukkit.event.block.BlockPlaceEvent event) {
		Player thePlayer = event.getPlayer();
		Material replaced = event.getBlockReplacedState().getType();
		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.CARVED_PUMPKIN) {
			ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
			if (item.getItemMeta() != null && item.getItemMeta().hasCustomModelData()) {
				int customModel = item.getItemMeta().getCustomModelData();
				SiegeEngine siegeEngine = null;
				// Search for match in custom model data value in defined siege engines
				for (SiegeEngine entry : SiegeEngines.definedSiegeEngines.values()) {
					if (entry.customModelID == customModel) {
						try {
							siegeEngine = entry.clone();
						} catch (CloneNotSupportedException e) {
						}
						break;
					} else {
						// if siege engine was broken during one of its firing stages
						if (entry.firingModelNumbers.contains(customModel)) {
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
								"§eSiege Engine cannot be placed within a 2.5 Block-Radius of other Siege Engines.");
					}
					event.setCancelled(true);
				}
			}
		}
	}

	public void Shoot(Entity player, long delay) {
		float actualDelay = delay;
		int counter = 0;
		if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()) == null) {
			for (Entity ent : player.getNearbyEntities(5, 5, 5)) {
				if (ent instanceof ArmorStand) {
					TakeControl(player, ent);
				}
			}
		}
		if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()) == null)
			return;
		List<Entity> entities = new ArrayList<>(SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()));
		for (Entity ent : entities) {
			if (ent == null || ent.isDead()) {
				SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()).remove(ent);
				continue;
			}
			if (((LivingEntity) ent).getEquipment().getHelmet().getType() != Material.CARVED_PUMPKIN) {
				SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()).remove(ent);
				continue;
			}
			double distance = player.getLocation().distance(ent.getLocation());
			if (distance >= Config.controlDistance) {
				SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()).remove(ent);
				continue;
			}
			SiegeEngine siege = SiegeEngines.activeSiegeEngines.get(ent.getUniqueId());
			if (counter > 4)
				return;
			if (ent == null || ent.isDead()) {
				SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()).remove(ent);
				continue;
			}
			if (siege.isLoaded()) {
				if (player instanceof Player) {
					siege.Fire(player, actualDelay, 1);
					actualDelay += delay;
				} else {
					siege.Fire(player, 15, 1);
				}
				counter++;
			} else {
				if (player instanceof Player) {
				}
			}

		}
	}

	public static ItemStack[] updateContents(Inventory inv, ItemStack m, int toRemove) {
		ItemStack[] contents = inv.getStorageContents();
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == null)
				continue;
			if (contents[i].isSimilar(m)) {
				int amountInInv = contents[i].getAmount();
				if (toRemove >= amountInInv) {
					contents[i].setType(Material.AIR);
					toRemove -= amountInInv;
				} else {
					contents[i].setAmount(amountInInv - toRemove);
					toRemove = 0;
					break;
				}
			}
			if (toRemove <= 0)
				break;
		}
		return contents;
	}

	private boolean pulledHeldAmmoFromPlayer(Player state, SiegeEngine siegeEngine) {
		ItemStack inventoryItem = state.getInventory().getItemInMainHand();
		if (siegeEngine.hasAmmunition() || !siegeEngine.hasPropellant()) {
			return false;
		}
		if (inventoryItem == null || inventoryItem.getType() == Material.AIR || !siegeEngine.hasPropellant()) {
			sendSiegeEngineHelpMSG(state, siegeEngine);
			return false;
		} else {
			for (ItemStack stack : siegeEngine.projectiles.keySet()) {
				if (inventoryItem == null)
					continue;
				if (siegeEngine.hasAmmunition()) {
					return true;
				}
				inventoryItem = inventoryItem.clone();
				if (!(stack.isSimilar(inventoryItem)) && (stack.getType() != Material.FIREWORK_ROCKET))
					continue;
				inventoryItem.setAmount(1);
				if (stack.getType() != Material.FIREWORK_ROCKET && stack.isSimilar(inventoryItem)
						&& siegeEngine.ammoHolder.loadedProjectile == 0) {
					siegeEngine.ammoHolder.loadedProjectile = 1;
					siegeEngine.ammoHolder.materialName = stack;
					state.getInventory().removeItem(inventoryItem);
					return true;
				}
				if (stack.getType() == Material.FIREWORK_ROCKET && inventoryItem.getType() == stack.getType()
						&& siegeEngine.ammoHolder.loadedProjectile == 0) {
					siegeEngine.ammoHolder.loadedProjectile = 1;
					siegeEngine.ammoHolder.materialName = inventoryItem;
					state.getInventory().removeItem(inventoryItem);
					FireworkProjectile proj = FireworkProjectile.getDefaultRocketShot(inventoryItem.clone());
					siegeEngine.projectiles.put(inventoryItem, proj);
					return true;
				}
				continue;
			}
			return false;
		}
	}

	private boolean pulledAmmoFromPlayer(Player state, SiegeEngine siegeEngine) {
		for (ItemStack inventoryItem : state.getInventory().getContents()) {
			for (ItemStack stack : siegeEngine.projectiles.keySet()) {
				if (inventoryItem == null)
					continue;
				if (siegeEngine.hasAmmunition()) {
					return true;
				}
				inventoryItem = inventoryItem.clone();
				inventoryItem.setAmount(1);
				if (!(stack.isSimilar(inventoryItem)) && (stack.getType() != Material.FIREWORK_ROCKET))
					continue;
				if (stack.getType() != Material.FIREWORK_ROCKET && stack.isSimilar(inventoryItem)
						&& siegeEngine.ammoHolder.loadedProjectile == 0) {
					siegeEngine.ammoHolder.loadedProjectile = 1;
					siegeEngine.ammoHolder.materialName = stack;
					state.getInventory().removeItem(inventoryItem);
					return true;
				}
				if (stack.getType() == Material.FIREWORK_ROCKET && inventoryItem.getType() == stack.getType()
						&& siegeEngine.ammoHolder.loadedProjectile == 0) {
					siegeEngine.ammoHolder.loadedProjectile = 1;
					siegeEngine.ammoHolder.materialName = inventoryItem;
					state.getInventory().removeItem(inventoryItem);
					FireworkProjectile proj = FireworkProjectile.getDefaultRocketShot(inventoryItem.clone());
					siegeEngine.projectiles.put(inventoryItem, proj);
					return true;
				}
				continue;
			}
		}
		return false;
	}

	private boolean pulledAmmoFromContainer(Location loc, SiegeEngine siegeEngine) {
		if (loc.getBlock().getType() == Material.BARREL || loc.getBlock().getType() == Material.CHEST) {
			Block inv = loc.getBlock();
			Container state = ((Container) inv.getState());
			for (ItemStack inventoryItem : state.getInventory().getContents()) {
				if (inventoryItem == null)
					continue;
				for (ItemStack stack : siegeEngine.projectiles.keySet()) {
					if (siegeEngine.hasAmmunition()) {
						return true;
					}
					if (!(stack.isSimilar(inventoryItem)) && (stack.getType() != Material.FIREWORK_ROCKET))
						continue;
					if (stack.getType() != Material.FIREWORK_ROCKET && stack.isSimilar(inventoryItem)
							&& siegeEngine.ammoHolder.loadedProjectile == 0) {
						siegeEngine.ammoHolder.loadedProjectile = 1;
						siegeEngine.ammoHolder.materialName = stack;
						if (inventoryItem.getAmount() - 1 > 0) {
							inventoryItem.setAmount(inventoryItem.getAmount() - 1);
						} else {
							inventoryItem.setType(Material.AIR);
							inventoryItem.setAmount(0);
						}
						return true;
					}
					if (stack.getType() == Material.FIREWORK_ROCKET && inventoryItem.getType() == stack.getType()
							&& siegeEngine.ammoHolder.loadedProjectile == 0) {
						siegeEngine.ammoHolder.loadedProjectile = 1;
						siegeEngine.ammoHolder.materialName = inventoryItem;
						if (inventoryItem.getAmount() - 1 > 0) {
							inventoryItem.setAmount(inventoryItem.getAmount() - 1);
						} else {
							inventoryItem.setType(Material.AIR);
							inventoryItem.setAmount(0);
						}
						FireworkProjectile proj = FireworkProjectile.getDefaultRocketShot(inventoryItem.clone());
						siegeEngine.projectiles.put(inventoryItem, proj);
						return true;
					}
					continue;
				}
			}
		}
		return false;
	}

	private boolean pulledPropellantFromContainer(Location loc, SiegeEngine siegeEngine) {
		if (loc.getBlock().getType() == Material.BARREL || loc.getBlock().getType() == Material.CHEST) {
			ItemStack stack = siegeEngine.fuelItem;
			stack.setAmount(1);
			Block inv = loc.getBlock();
			Container state = ((Container) inv.getState());
			if (!siegeEngine.canLoadFuel()) {
				return false;
			}
			if (!SiegeEnginesUtil.hasItem((Inventory) state.getInventory(), stack)) {
				return false;
			}
			state.getInventory().removeItem(stack);
			siegeEngine.ammoHolder.loadedFuel += 1;
			return true;
		}
		return false;
	}

	private void sendPropellantStatusMSG(Player player, SiegeEngine siegeEngine) {
		player.sendMessage("§eReloaded propellant");
		if (!siegeEngine.canLoadFuel()) {
			player.sendMessage("§ePropellant is Full! Level: §6(" + siegeEngine.ammoHolder.loadedFuel + "/"
					+ siegeEngine.maxFuel + ")");
		} else {
			player.sendMessage(
					"§ePropellant level is: §6(" + siegeEngine.ammoHolder.loadedFuel + "/" + siegeEngine.maxFuel + ")");
		}
	}

	private void sendSiegeEngineHelpMSG(Player player, SiegeEngine siegeEngine) {
		if (!siegeEngine.canLoadFuel()) {
			player.sendMessage("§ePropellant is Full! Level: §6(" + siegeEngine.ammoHolder.loadedFuel + "/"
					+ siegeEngine.maxFuel + ")");
			if (siegeEngine.ammoHolder.loadedProjectile == 0) {
				player.sendMessage("§eLoad Ammunition by Right Clicking the Siege Engine with any Valid Projectilie!");
			}
		} else {
			player.sendMessage(
					"§ePropellant level is: §6(" + siegeEngine.ammoHolder.loadedFuel + "/" + siegeEngine.maxFuel + ")");
		}
	}

	@EventHandler
	public void interact(PlayerInteractEvent event) {
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
						if (siegeEngine != null && siegeEngine.enabled && !(siegeEngineEntity.isDead())
								&& siegeEngine.isLoaded()) {
							if (siegeEngine.setModelNumberWhenFullyLoaded && siegeEngine.canLoadFuel()) {
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
			autoReload(player);
		}
		
	}

	private boolean autoReload(Player player) {
		// Player is sneaking, thus attempt to reload
		if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {

			// get the list of SiegeEngine entities currently being controlled by the player
			List<Entity> siegeEngineEntities = SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId());
			if (siegeEngineEntities == null) {
				return false;
			}

			// for each SiegeEngine entity currently being controlled by the player
			for (Entity siegeEngineEntity : siegeEngineEntities) {
				if ((siegeEngineEntity.isDead())) {
					continue;
				}
				// Validate for SiegeEngine and get object
				if (SiegeEngines.activeSiegeEngines.containsKey(siegeEngineEntity.getUniqueId())) {
					SiegeEngine siegeEngine = SiegeEngines.activeSiegeEngines.get(siegeEngineEntity.getUniqueId());
					if (siegeEngine == null || !siegeEngine.enabled) {
						continue;
					}
					if (siegeEngine.hasAmmunition()) {
						continue;
					}
					// Checks for ammo in nearby container and loads if found
					if (pulledAmmoFromContainer(siegeEngine.entity.getLocation(), siegeEngine)) {
						player.sendMessage("§eReloaded ammunition!");
						continue;
					}
					if (pulledAmmoFromContainer(
							siegeEngine.entity.getLocation().getBlock().getRelative(0, -1, 0).getLocation(),
							siegeEngine)) {
						player.sendMessage("§eReloaded ammunition!");
						continue;
					}
					if (pulledAmmoFromContainer(
							siegeEngine.entity.getLocation().getBlock().getRelative(0, 1, 0).getLocation(),
							siegeEngine)) {
						player.sendMessage("§eReloaded ammunition!");
						continue;
					}
					// Search player's inv for ammo because it was not found in a nearby container
					if (pulledAmmoFromPlayer(player, siegeEngine)) {
						player.sendMessage("§eReloaded ammunition!");
						continue;
					}
					if (!siegeEngine.hasAmmunition()) {
						player.sendMessage("§eCould not reload ammunition.");
					}
					// Old Non-Firework Compatible
					// event.getPlayer().getInventory().getContents()
					for (ItemStack inventoryItem : player.getInventory().getContents()) {
						for (ItemStack stack : siegeEngine.projectiles.keySet()) {
							if (siegeEngine.hasAmmunition()) {
								continue;
							}
							if (!SiegeEnginesUtil.hasItem(player.getInventory(), stack))
								continue;
							if (stack.isSimilar(inventoryItem) && siegeEngine.ammoHolder.loadedProjectile == 0) {
								siegeEngine.ammoHolder.loadedProjectile = 1;
								siegeEngine.ammoHolder.materialName = stack;
								inventoryItem.setAmount(inventoryItem.getAmount() - 1);
								break;
							}
						}
					}
				}
			}

			for (Entity siegeEngineEntity : siegeEngineEntities) {
				if ((siegeEngineEntity.isDead()))
					continue;
				if (SiegeEngines.activeSiegeEngines.containsKey(siegeEngineEntity.getUniqueId())) {
					SiegeEngine siegeEngine = SiegeEngines.activeSiegeEngines.get(siegeEngineEntity.getUniqueId());
					if (siegeEngine == null || !siegeEngine.enabled) {
						continue;
					}
					// Checks for propellant in nearby container and loads if found
					if (pulledPropellantFromContainer(siegeEngine.entity.getLocation(), siegeEngine)) {
						sendPropellantStatusMSG(player, siegeEngine);
						continue;
					}
					if (pulledPropellantFromContainer(
							siegeEngine.entity.getLocation().getBlock().getRelative(0, -1, 0).getLocation(),
							siegeEngine)) {
						sendPropellantStatusMSG(player, siegeEngine);
						continue;
					}
					if (pulledPropellantFromContainer(
							siegeEngine.entity.getLocation().getBlock().getRelative(0, 1, 0).getLocation(),
							siegeEngine)) {
						sendPropellantStatusMSG(player, siegeEngine);
						continue;
					}
					// Check player's inv for propellant
					if (siegeEngine.canLoadFuel()) {
						ItemStack stack = siegeEngine.fuelItem;
						if (SiegeEnginesUtil.hasItem((Inventory) player.getInventory(), stack)) {
							siegeEngine.ammoHolder.loadedFuel += 1;
							stack.setAmount(1);
							player.getInventory().removeItem(stack);
							sendPropellantStatusMSG(player, siegeEngine);
							if (!siegeEngine.canLoadFuel())
								continue;
						}
					}
					if (!siegeEngine.hasPropellant()) {
						player.sendMessage("§eCould not load propellant.");
					} else {
						continue;
					}
				}
			}
			return false;
		}
		return true;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onSignChange(SignChangeEvent event) {
		@SuppressWarnings("deprecation")
		String topline = event.getLine(0);
		if (topline == null)
			topline = "";
		Player player = event.getPlayer();
		String toplinetrimmed = topline.trim();
		if (toplinetrimmed.equalsIgnoreCase("[Cannon]")) {
			SaveCannons(player, event.getBlock());
		}

	}

	@EventHandler
	public void damage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof ArmorStand) {
			if (event.getDamager() instanceof Projectile) {
				event.setDamage(0);
				event.setCancelled(true);
			}
		}
	}

	public static boolean isSiegeEngine(Entity entity, boolean add) {
		LivingEntity living = (LivingEntity) entity;
		if (living.getEquipment().getHelmet() != null
				&& living.getEquipment().getHelmet().getType() == Material.CARVED_PUMPKIN) {
			if (living.getEquipment() == null || living.getEquipment().getHelmet() == null
					|| living.getEquipment().getHelmet().getItemMeta() == null) {
				return false;
			}
			final List<UUID> keys = new ArrayList<>(SiegeEngines.siegeEngineEntitiesPerPlayer.keySet());
			int numPilots = 0;
			for (UUID player : keys) {
				if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(player) != null) {
					if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(player).contains(entity)) {
						final List<Entity> entities = new ArrayList<>(
								SiegeEngines.siegeEngineEntitiesPerPlayer.get(player));
						numPilots++;
						if (numPilots > 1) {
							entities.remove(entity);
						}
						SiegeEngines.siegeEngineEntitiesPerPlayer.put(player, entities);
					}
				}
				// Can only have one pilot
			}

			ArmorStand stand = (ArmorStand) entity;
			SiegeEngine equip;
			stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
			stand.setBasePlate(true);

			if (SiegeEngines.activeSiegeEngines.containsKey(entity.getUniqueId())) {
				equip = SiegeEngines.activeSiegeEngines.get(entity.getUniqueId());
				if (equip == null || !equip.enabled) {
					return false;
				}
			} else {
				equip = SiegeEnginesUtil.createCloneFromCustomModelData(
						living.getEquipment().getHelmet().getItemMeta().getCustomModelData());
				if (equip == null || !equip.enabled) {
					return false;
				}
				equip.ammoHolder = new SiegeEngineAmmoHolder();
				equip.entity = entity;
				equip.entityId = entity.getUniqueId();
			}
			stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
			stand.setBasePlate(true);
			if (numPilots > 1) {
				return false;
			}
			if (add)
				SiegeEngines.activeSiegeEngines.put(entity.getUniqueId(), equip);
			// player.sendMessage("§eNow controlling the equipment.");
			return true;
		}
		return false;
	}

	public static void TakeControl(Entity player, Entity entity) {
		LivingEntity living = (LivingEntity) entity;
		if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
			List<Entity> entities = SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId());
			if (entities.contains(entity)) {
				return;
			}
		}
		// Can only have one pilot
		final List<UUID> keys = new ArrayList<>(SiegeEngines.siegeEngineEntitiesPerPlayer.keySet());
		int numPilots = 0;
		for (UUID uuid : keys) {
			if (uuid.equals(player.getUniqueId())) {
				continue;
			}
			numPilots++;

		}
		if (numPilots > 1) {
			if (player instanceof Player)
				((Player) player).sendMessage("§eOnly one Player may command this Siege Engine!");
			return;
		}

		if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()) != null) {
			if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId())
					.size() >= Config.maxSiegeEnginesControlled) {
				if (player instanceof Player) {
					((Player) player).sendMessage("§eYou are already commanding too many Siege Engines!");
				}
				return;
			}
		}
		if (living.getEquipment().getHelmet() != null
				&& living.getEquipment().getHelmet().getType() == Material.CARVED_PUMPKIN) {
			if (living.getEquipment() == null || living.getEquipment().getHelmet() == null
					|| living.getEquipment().getHelmet().getItemMeta() == null) {
				return;
			}

			ArmorStand stand = (ArmorStand) entity;
			SiegeEngine equip;
			stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
			stand.setBasePlate(true);

			if (SiegeEngines.activeSiegeEngines.containsKey(entity.getUniqueId())) {
				equip = SiegeEngines.activeSiegeEngines.get(entity.getUniqueId());
				if (equip == null || !equip.enabled) {
					return;
				}
			} else {
				equip = SiegeEnginesUtil.createCloneFromCustomModelData(
						living.getEquipment().getHelmet().getItemMeta().getCustomModelData());
				if (equip == null || !equip.enabled) {
					return;
				}
				equip.ammoHolder = new SiegeEngineAmmoHolder();
				equip.entity = entity;
				equip.entityId = entity.getUniqueId();
			}
			stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
			stand.setBasePlate(true);
			if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
				List<Entity> entities = SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId());
				entities.add(entity);
				SiegeEngines.siegeEngineEntitiesPerPlayer.put(player.getUniqueId(), entities);
			} else {
				List<Entity> newList = new ArrayList<Entity>();
				newList.add(entity);
				SiegeEngines.siegeEngineEntitiesPerPlayer.put(player.getUniqueId(), newList);
			}
			SiegeEngines.activeSiegeEngines.put(entity.getUniqueId(), equip);
			// player.sendMessage("§eNow controlling the equipment.");
			if (player instanceof Player) {
				((Player) player).sendMessage("§eYou are now commanding a total of "
						+ SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()).size()
						+ " Siege Engines!");
			}
		}
	}

	public void SaveCannons(Player player, Block block) {
		List<String> Ids = new ArrayList<String>();
		if (!SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
			return;
		}
		for (Entity ent : SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId())) {
			if (ent.isDead()) {
				continue;
			}
			Ids.add(ent.getUniqueId().toString());
		}
		for (Entity ent : SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId())) {
			if (ent.isDead()) {
				continue;
			}
			DoAimUp(ent, 0, player);
		}
	}

	public void DoAimUp(Entity ent, float amount, Entity player) {
		Location loc = ent.getLocation();
		ArmorStand stand = (ArmorStand) ent;
		// player.sendMessage(String.format("" + loc.getPitch()));
		if (loc.getPitch() == -85 || loc.getPitch() - amount < -85) {
			return;
		}
		loc.setPitch((float) (loc.getPitch() - amount));
		SiegeEngine equipment = SiegeEngines.activeSiegeEngines.get(ent.getUniqueId());
		if (equipment != null) {
			if (player instanceof Player)
				equipment.ShowFireLocation(player);
			if (equipment.rotateStandHead) {
				stand.setHeadPose(new EulerAngle(loc.getDirection().getY() * (-1), 0, 0));
			}
			ent.teleport(loc);
		}
	}

	public void DoAimDown(Entity ent, float amount, Entity player) {
		Location loc = ent.getLocation();
		ArmorStand stand = (ArmorStand) ent;
		// player.sendMessage(String.format("" + loc.getPitch()));
		if (loc.getPitch() == 85 || loc.getPitch() - amount < 65) {
			return;
		}
		loc.setPitch((float) (loc.getPitch() - amount));
		SiegeEngine equipment = SiegeEngines.activeSiegeEngines.get(ent.getUniqueId());
		if (equipment != null) {
			if (player instanceof Player)
				equipment.ShowFireLocation(player);
			if (equipment.rotateStandHead) {
				stand.setHeadPose(new EulerAngle(loc.getDirection().getY() * (-1), 0, 0));
			}
			ent.teleport(loc);
		}
	}

	public void FirstShotDoAimDown(Entity ent, float amount, Entity player) {
		Location loc = ent.getLocation();
		ArmorStand stand = (ArmorStand) ent;
		// player.sendMessage(String.format("" + loc.getPitch()));
		if (loc.getPitch() == 85 || loc.getPitch() + amount > 85) {
			return;
		}
		SiegeEngine equipment = SiegeEngines.activeSiegeEngines.get(ent.getUniqueId());
		if (equipment != null) {
			if (player instanceof Player) {
				equipment.ShowFireLocation((Player) player);
			}
			if (equipment.rotateStandHead) {
				stand.setHeadPose(new EulerAngle(loc.getDirection().getY() * (-1), 0, 0));

			}
		}
		loc.setPitch((float) (loc.getPitch() + amount));

		ent.teleport(loc);
	}

	NamespacedKey key = new NamespacedKey(SiegeEngines.getInstance(), "siege_engines");

	@EventHandler
	public void DeathEvent(EntityDeathEvent event) {
		Boolean removeStands = false;
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
							if (siegeEngine.firingModelNumbers.contains(i.getItemMeta().getCustomModelData())) {
								removeStands = true;
								break;
							}
						}
					}
				}
			}
			if (removeStands) {
				for (UUID key : SiegeEngines.siegeEngineEntitiesPerPlayer.keySet()) {
					if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(key) != null) {
						final List<Entity> entities = new ArrayList<>(
								SiegeEngines.siegeEngineEntitiesPerPlayer.get(key));
						if (entities.contains(event.getEntity())) {
							entities.remove(event.getEntity());
							SiegeEngines.siegeEngineEntitiesPerPlayer.put(key, entities);
						}
					}
				}
				for (ItemStack i : items) {
					if (i.getType() == Material.ARMOR_STAND) {
						i.setAmount(0);
						return;
					}
				}
			}
		}
	}

	public void AimUp(Entity player, float amount) {

		for (Entity ent : SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId())) {
			if (ent.isDead()) {
				continue;
			}
			DoAimUp(ent, amount, player);
		}
	}

	public void AimDown(Entity player, float amount) {

		for (Entity ent : SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId())) {
			if (ent.isDead()) {
				continue;
			}
			DoAimDown(ent, amount, player);
		}
	}

	public void LoadCannonsWithPowder(Entity player) {
		for (Entity ent : SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId())) {
			if (ent.isDead()) {
				continue;
			}

			SiegeEngine equipment = SiegeEngines.activeSiegeEngines.get(ent.getUniqueId());
			if (equipment != null) {
				equipment.loadFuel(player);
			}
		}
	}

	public void LoadCannonsWithProjectile(Entity player, ItemStack projectile) {
		for (Entity ent : SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId())) {
			if (ent.isDead()) {
				continue;
			}
			SiegeEngine equipment = SiegeEngines.activeSiegeEngines.get(ent.getUniqueId());
			if (equipment != null) {
				equipment.LoadProjectile(player, projectile);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerClickSign(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getClickedBlock() != null && event.getClickedBlock().getType().toString().contains("SIGN")) {
			Sign sign = (Sign) event.getClickedBlock().getState();
			if (sign.getLine(0).equalsIgnoreCase("[Fire]") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (!SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
					return;
				}
				try {
					Long delay = Long.parseLong(sign.getLine(1));
					if (delay < 6) {
						delay = 6l;
					}
					Shoot((Entity) player, delay);
				} catch (Exception e) {
					Shoot((Entity) player, 6);
				}
				return;
			}
			if (sign.getLine(0).equalsIgnoreCase("[Load]")) {
				if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
					LoadCannonsWithPowder((Entity) player);
					return;
				}
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					// load proj
					ItemStack itemInHand = player.getInventory().getItemInMainHand();
					if (itemInHand == null) {
						return;
					}

					LoadCannonsWithProjectile((Entity) player, player.getInventory().getItemInMainHand());
					return;
				}
			}

			if (sign.getLine(0).equalsIgnoreCase("[Aim]")) {
				if (!SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
					return;
				}
				float amount;
				try {
					amount = Float.parseFloat(sign.getLine(1));
					if (player.isSneaking()) {
						AimDown((Entity) player, amount);
					} else {
						AimUp((Entity) player, amount);
					}
				} catch (Exception e) {
					player.sendMessage("§eCould not parse number on second line.");
				}

				return;
			}

			if (sign.getLine(0).equalsIgnoreCase("[Cannon]")) {
				if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
					PlayerHandler.releasePlayerSiegeEngine(player,null);
					return;
				}

				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (player.isSneaking()) {
						SaveCannons(player, event.getClickedBlock());
						return;
					}

					NamespacedKey key = new NamespacedKey(SiegeEngines.getInstance(), "siege_engines");
					TileState state = (TileState) sign.getBlock().getState();
					SiegeEngines.siegeEngineEntitiesPerPlayer.remove(player.getUniqueId());
					List<UUID> temp = new ArrayList<UUID>();
					if (!state.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
						return;
					}
					String[] split = state.getPersistentDataContainer().get(key, PersistentDataType.STRING)
							.replace("[", "").replace("]", "").split(",");
					for (String s : split) {
						temp.add(UUID.fromString(s.trim()));
					}
					for (UUID Id : temp) {
						Entity ent = Bukkit.getEntity(Id);
						if (ent != null) {
							TakeControl(player, ent);
						}
					}
					player.sendMessage("§eNow controlling nearby Siege Engines.");
					return;
				}
			}

		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onEntityClick(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getInventory().getItemInMainHand();
		Entity entity = event.getRightClicked();
		if (entity == null) {
			return;
		}
		if (entity.getType() == EntityType.ARMOR_STAND) {
			if ((itemInHand.getType() == Material.AIR || itemInHand == null))  {
				if (player.isSneaking()) {
					if (isSiegeEngine(entity, false)) PlayerHandler.releasePlayerSiegeEngine(player,entity);
					else PlayerHandler.releasePlayerSiegeEngine(player,null);
					if (SiegeEngines.activeSiegeEngines.containsKey(entity.getUniqueId())) {
						SiegeEngines.activeSiegeEngines.remove(entity.getUniqueId());
					}
					player.sendMessage("§eReleased this SiegeEngine!");
					return;
				}
			}
			if (!(player.isSneaking()) && itemInHand.getType() == Config.controlItem) {
				if (isSiegeEngine(entity, false)) TakeControl(player, entity);
			}
			/*if (itemInHand.getType() == Config.controlItem) {
				if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()) != null) {
					if (!(SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()).contains(entity))) {
						player.sendMessage("§eNow commanding this Siege Engine.");
						TakeControl(player, entity);
					}
				} else {
					player.sendMessage("§eNow commanding this Siege Engine.");
					TakeControl(player, entity);
				}
			}*/
			if (SiegeEngines.activeSiegeEngines.containsKey(entity.getUniqueId())) {
				SiegeEngine siegeEngine = SiegeEngines.activeSiegeEngines.get(entity.getUniqueId());
				event.setCancelled(true);
				ItemStack stack = siegeEngine.fuelItem;
				if (itemInHand.getType() == stack.getType()) {
					if (siegeEngine.canLoadFuel()) {
						if (itemInHand.isSimilar(stack)) {
							siegeEngine.ammoHolder.loadedFuel += 1;
							stack.setAmount(1);
							player.getInventory().removeItem(stack);
							sendSiegeEngineHelpMSG(player, siegeEngine);
							// If fully loaded && requires change in animation when loaded (i.e. ballista)
							if (!siegeEngine.canLoadFuel() && siegeEngine.setModelNumberWhenFullyLoaded) {
								SiegeEnginesUtil.UpdateEntityIdModel(siegeEngine.entity, siegeEngine.preFireModelNumber, siegeEngine.worldName);
							}
						}
					}
				}
				if (pulledHeldAmmoFromPlayer(player, siegeEngine)) {
					player.sendMessage("§eAdded ammunition to this Siege Engine.");
					return;
				}
				if (itemInHand == null || itemInHand.getType() == Material.AIR
						|| itemInHand.getType() == Config.controlItem) {
					if (!pulledPropellantFromContainer(siegeEngine.entity.getLocation(), siegeEngine)) {
						sendSiegeEngineHelpMSG(player, siegeEngine);
						return;
					}
					if (!pulledPropellantFromContainer(
							siegeEngine.entity.getLocation().getBlock().getRelative(0, -1, 0).getLocation(), siegeEngine)) {
						sendSiegeEngineHelpMSG(player, siegeEngine);
						return;
					}
					if (!pulledPropellantFromContainer(
							siegeEngine.entity.getLocation().getBlock().getRelative(0, 1, 0).getLocation(), siegeEngine)) {
						sendSiegeEngineHelpMSG(player, siegeEngine);
						return;
					}
					if (pulledAmmoFromContainer(siegeEngine.entity.getLocation(), siegeEngine)) {
						player.sendMessage("§eAdded ammunition to this Siege Engine.");
						return;
					}
					if (pulledAmmoFromContainer(
							siegeEngine.entity.getLocation().getBlock().getRelative(0, -1, 0).getLocation(), siegeEngine)) {
						player.sendMessage("§eAdded ammunition to this Siege Engine.");
						return;
					}
					if (pulledAmmoFromContainer(
							siegeEngine.entity.getLocation().getBlock().getRelative(0, 1, 0).getLocation(), siegeEngine)) {
						player.sendMessage("§eAdded ammunition to this Siege Engine.");
						return;
					}
				}
			}
		}
	}
}
