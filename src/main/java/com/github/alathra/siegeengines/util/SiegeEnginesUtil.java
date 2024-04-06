package com.github.alathra.siegeengines.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;

import com.github.alathra.siegeengines.SiegeEngine;
import com.github.alathra.siegeengines.SiegeEngineAmmoHolder;
import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.SiegeEnginesLogger;
import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.listeners.PlayerHandler;
import com.github.alathra.siegeengines.projectile.FireworkProjectile;
import com.github.alathra.siegeengines.projectile.PotionProjectile;

public class SiegeEnginesUtil {
	
	public static final ItemStack DEFAULT_ROCKET = new ItemStack(Material.FIREWORK_ROCKET);
	public static void UpdateEntityIdModel(Entity ent, int modelNumber, String WorldName) {
        if (ent instanceof LivingEntity) {
            LivingEntity liv = (LivingEntity) ent;
            ItemStack Helmet = liv.getEquipment().getHelmet();
            if (Helmet != null) {
                ItemMeta meta = Helmet.getItemMeta();
                meta.setCustomModelData(modelNumber);
                Helmet.setItemMeta(meta);
                liv.getEquipment().setHelmet(Helmet);
            }
        }
    }
	
	public static FixedMetadataValue addMetaDataValue(Object value) {
        return new FixedMetadataValue(Bukkit.getServer().getPluginManager().getPlugin("SiegeEngines"), value);
    }
	
	public static String convertTime(long time) {
        long days = TimeUnit.MILLISECONDS.toDays(time);
        time -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(time);
        time -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
        String timeLeftFormatted = String.format("§e" + minutes + " Minutes " + seconds + " Seconds§f");
        return timeLeftFormatted;
    }
	
	public static SiegeEngine createCloneFromCustomModelData(Integer ModelId) {
        try {
            return SiegeEngines.definedSiegeEngines.get(ModelId).clone();
        } catch (CloneNotSupportedException e) {

        }
        return null;
    }
	
	public static boolean isSiegeEngine(Entity entity, boolean add) {
		LivingEntity living = (LivingEntity) entity;
		if (living.getEquipment().getHelmet() != null
				&& living.getEquipment().getHelmet().getType() == Material.CARVED_PUMPKIN) {
			if (living.getEquipment() == null || living.getEquipment().getHelmet() == null
					|| living.getEquipment().getHelmet().getItemMeta() == null) {
				return false;
			}
			
			ArmorStand stand = (ArmorStand) entity;
			SiegeEngine siegeEngines;
			stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
			stand.setBasePlate(true);

			SiegeEnginesLogger.debug("ACTIVE ENGINES : " + SiegeEngines.activeSiegeEngines);
			if (SiegeEngines.activeSiegeEngines.containsKey(entity.getUniqueId())) {
				siegeEngines = SiegeEngines.activeSiegeEngines.get(entity.getUniqueId());
				if (siegeEngines == null || !siegeEngines.getEnabled()) {
					return false;
				}
			} else {
				siegeEngines = SiegeEnginesUtil.createCloneFromCustomModelData(
						living.getEquipment().getHelmet().getItemMeta().getCustomModelData());
				if (siegeEngines == null || !siegeEngines.getEnabled()) {
					return false;
				}
				siegeEngines.setAmmoHolder(new SiegeEngineAmmoHolder());
				siegeEngines.setEntity(entity);
				siegeEngines.setEntityId(entity.getUniqueId());
			}
			stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
			stand.setBasePlate(true);
			// player.sendMessage("§eNow controlling the equipment.");
			if (add) SiegeEngines.activeSiegeEngines.put(entity.getUniqueId(), siegeEngines);
			return true;
		}
		return false;
	}
	
	public static void loadSiegeEngineWithProjectile(Entity player, ItemStack projectile) {
		for (Entity ent : SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId())) {
			if (ent.isDead()) {
				continue;
			}
			SiegeEngine siegeEngine = SiegeEngines.activeSiegeEngines.get(ent.getUniqueId());
			if (siegeEngine != null) {
				siegeEngine.LoadProjectile(player, projectile);
			}
		}
	}
	
	public static void loadSiegeEngineWithPropellant(Entity player) {
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
	
	public static void shoot(Entity player, long delay) {
		float actualDelay = delay;
		int counter = 0;
		if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()) == null) {
			for (Entity ent : player.getNearbyEntities(5, 5, 5)) {
				if (ent instanceof ArmorStand) {
					takeControl(player, ent);
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
	
	public static void takeControl(Entity player, Entity entity) {
		LivingEntity living = (LivingEntity) entity;
		if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
			List<Entity> entities = SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId());
			if (entities.contains(entity)) {
				if (player instanceof Player) {
					((Player) player).sendMessage("§eYou are already commanding this Siege Engine!");
				}
				return;
			}
		}
		// Can only have one pilot
		final HashSet<UUID> keys = new HashSet<>(SiegeEngines.siegeEngineEntitiesPerPlayer.keySet());
		int numPilots = 0;
		for (UUID uuid : keys) {
			if (uuid.equals(player.getUniqueId())) {
				continue;
			}
			if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(uuid)) {
				if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).contains(entity)) {
					numPilots++;
				}
			}
		}
		if (numPilots > 1) {
			if (player instanceof Player) ((Player) player).sendMessage("§eOnly one Player may command this Siege Engine!");
			return;
		}

		if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
			SiegeEnginesLogger.debug("CURRENT ENGINES : " + SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()));
			if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId())
					.size() > Config.maxSiegeEnginesControlled) {
				if (player instanceof Player) {
					((Player) player).sendMessage("§eYou are commanding too many Siege Engines!");
					PlayerHandler.releasePlayerSiegeEngine(((Player) player),entity);
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
				if (equip == null || !equip.getEnabled()) {
					return;
				}
			} else {
				equip = SiegeEnginesUtil.createCloneFromCustomModelData(
						living.getEquipment().getHelmet().getItemMeta().getCustomModelData());
				if (equip == null || !equip.getEnabled()) {
					return;
				}
				equip.setAmmoHolder(new SiegeEngineAmmoHolder());
				equip.setEntity(entity);
				equip.setEntityId(entity.getUniqueId());
			}
			stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
			stand.setBasePlate(true);
			if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
				List<Entity> entities = SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId());
				entities.add(entity);
				SiegeEngines.siegeEngineEntitiesPerPlayer.remove(player.getUniqueId());
				SiegeEngines.siegeEngineEntitiesPerPlayer.put(player.getUniqueId(), entities);
			} else {
				List<Entity> newList = new ArrayList<Entity>();
				newList.add(entity);
				SiegeEngines.siegeEngineEntitiesPerPlayer.remove(player.getUniqueId());
				SiegeEngines.siegeEngineEntitiesPerPlayer.put(player.getUniqueId(), newList);
			}
			SiegeEnginesLogger.debug("NEW ENGINES : " + SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()));
			SiegeEngines.activeSiegeEngines.put(entity.getUniqueId(), equip);
			// player.sendMessage("§eNow controlling the equipment.");
			if (player instanceof Player) {
				((Player) player).sendMessage("§eYou are now commanding a total of "
						+ SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()).size()
						+ " Siege Engines!");
			}
		}
	}
	
	public static void aimUp(Entity player, float amount) {

		for (Entity ent : SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId())) {
			if (ent.isDead()) {
				continue;
			}
			doAimUp(ent, amount, player);
		}
	}

	public static void aimDown(Entity player, float amount) {

		for (Entity ent : SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId())) {
			if (ent.isDead()) {
				continue;
			}
			doAimDown(ent, amount, player);
		}
	}
	
	public static void doAimUp(Entity ent, float amount, Entity player) {
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
			if (equipment.isRotateStandHead()) {
				stand.setHeadPose(new EulerAngle(loc.getDirection().getY() * (-1), 0, 0));
			}
			ent.teleport(loc);
		}
	}

	public static void doAimDown(Entity ent, float amount, Entity player) {
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
			if (equipment.isRotateStandHead()) {
				stand.setHeadPose(new EulerAngle(loc.getDirection().getY() * (-1), 0, 0));
			}
			ent.teleport(loc);
		}
	}
	
	public static void firstShotDoAimDown(Entity ent, float amount, Entity player) {
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
			if (equipment.isRotateStandHead()) {
				stand.setHeadPose(new EulerAngle(loc.getDirection().getY() * (-1), 0, 0));

			}
		}
		loc.setPitch((float) (loc.getPitch() + amount));

		ent.teleport(loc);
	}
	
	public static boolean pulledHeldAmmoFromPlayer(Player state, SiegeEngine siegeEngine) {
		ItemStack inventoryItem = state.getInventory().getItemInMainHand();
		if (siegeEngine.hasAmmunition() || !siegeEngine.hasPropellant()) {
			return false;
		}
		if (inventoryItem == null || inventoryItem.getType() == Material.AIR || !siegeEngine.hasPropellant()) {
			sendSiegeEngineHelpMSG(state, siegeEngine);
			return false;
		} else {
			for (ItemStack stack : siegeEngine.getProjectiles().keySet()) {
				if (inventoryItem == null)
					continue;
				if (siegeEngine.hasAmmunition()) {
					return true;
				}
				inventoryItem = inventoryItem.clone();
				if (!(stack.isSimilar(inventoryItem)) && (stack.getType() != Material.FIREWORK_ROCKET)
						&& (stack.getType() != Material.SPLASH_POTION))
					continue;
				inventoryItem.setAmount(1);
				if (stack.getType() != Material.FIREWORK_ROCKET && stack.isSimilar(inventoryItem)
						&& siegeEngine.getAmmoHolder().getLoadedProjectile() == 0) {
					siegeEngine.getAmmoHolder().setLoadedProjectile(1);
					siegeEngine.getAmmoHolder().setMaterialName(stack);
					state.getInventory().removeItem(inventoryItem);
					return true;
				}
				if (stack.getType() == Material.FIREWORK_ROCKET && inventoryItem.getType() == stack.getType()
						&& siegeEngine.getAmmoHolder().getLoadedProjectile() == 0) {
					siegeEngine.getAmmoHolder().setLoadedProjectile(1);
					siegeEngine.getAmmoHolder().setMaterialName(inventoryItem);
					state.getInventory().removeItem(inventoryItem);
					FireworkProjectile proj = FireworkProjectile.getDefaultRocketShot(inventoryItem.clone());
					siegeEngine.getProjectiles().put(inventoryItem, proj);
					return true;
				}
				if (stack.getType() == Material.SPLASH_POTION && inventoryItem.getType() == stack.getType()
						&& siegeEngine.getAmmoHolder().getLoadedProjectile() == 0) {
					siegeEngine.getAmmoHolder().setLoadedProjectile(1);
					siegeEngine.getAmmoHolder().setMaterialName(inventoryItem);
					state.getInventory().removeItem(inventoryItem);
					PotionProjectile proj = new PotionProjectile(inventoryItem);
					siegeEngine.getProjectiles().put(inventoryItem, proj);
					return true;
				}
				continue;
			}
			return false;
		}
	}

	public static boolean pulledAmmoFromPlayer(Player state, SiegeEngine siegeEngine) {
		for (ItemStack inventoryItem : state.getInventory().getContents()) {
			for (ItemStack stack : siegeEngine.getProjectiles().keySet()) {
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
						&& siegeEngine.getAmmoHolder().getLoadedProjectile() == 0) {
					siegeEngine.getAmmoHolder().setLoadedProjectile(1);
					siegeEngine.getAmmoHolder().setMaterialName(stack);
					state.getInventory().removeItem(inventoryItem);
					return true;
				}
				if (stack.getType() == Material.FIREWORK_ROCKET && inventoryItem.getType() == stack.getType()
						&& siegeEngine.getAmmoHolder().getLoadedProjectile() == 0) {
					siegeEngine.getAmmoHolder().setLoadedProjectile(1);
					siegeEngine.getAmmoHolder().setMaterialName(inventoryItem);
					state.getInventory().removeItem(inventoryItem);
					FireworkProjectile proj = FireworkProjectile.getDefaultRocketShot(inventoryItem.clone());
					siegeEngine.getProjectiles().put(inventoryItem, proj);
					return true;
				}
				continue;
			}
		}
		return false;
	}

	public static boolean pulledAmmoFromContainer(Location loc, SiegeEngine siegeEngine) {
		if (loc.getBlock().getType() == Material.BARREL || loc.getBlock().getType() == Material.CHEST) {
			Block inv = loc.getBlock();
			Container state = ((Container) inv.getState());
			for (ItemStack inventoryItem : state.getInventory().getContents()) {
				if (inventoryItem == null)
					continue;
				for (ItemStack stack : siegeEngine.getProjectiles().keySet()) {
					if (siegeEngine.hasAmmunition()) {
						return true;
					}
					if (!(stack.isSimilar(inventoryItem)) && (stack.getType() != Material.FIREWORK_ROCKET))
						continue;
					if (stack.getType() != Material.FIREWORK_ROCKET && stack.isSimilar(inventoryItem)
							&& siegeEngine.getAmmoHolder().getLoadedProjectile() == 0) {
						siegeEngine.getAmmoHolder().setLoadedProjectile(1);
						siegeEngine.getAmmoHolder().setMaterialName(stack);
						if (inventoryItem.getAmount() - 1 > 0) {
							inventoryItem.setAmount(inventoryItem.getAmount() - 1);
						} else {
							inventoryItem.setType(Material.AIR);
							inventoryItem.setAmount(0);
						}
						return true;
					}
					if (stack.getType() == Material.FIREWORK_ROCKET && inventoryItem.getType() == stack.getType()
							&& siegeEngine.getAmmoHolder().getLoadedProjectile() == 0) {
						siegeEngine.getAmmoHolder().setLoadedProjectile(1);
						siegeEngine.getAmmoHolder().setMaterialName(inventoryItem);
						if (inventoryItem.getAmount() - 1 > 0) {
							inventoryItem.setAmount(inventoryItem.getAmount() - 1);
						} else {
							inventoryItem.setType(Material.AIR);
							inventoryItem.setAmount(0);
						}
						FireworkProjectile proj = FireworkProjectile.getDefaultRocketShot(inventoryItem.clone());
						siegeEngine.getProjectiles().put(inventoryItem, proj);
						return true;
					}
					continue;
				}
			}
		}
		return false;
	}

	public static boolean pulledPropellantFromContainer(Location loc, SiegeEngine siegeEngine) {
		if (loc.getBlock().getType() == Material.BARREL || loc.getBlock().getType() == Material.CHEST) {
			ItemStack stack = siegeEngine.getFuelItem();
			stack.setAmount(1);
			Block inv = loc.getBlock();
			Container state = ((Container) inv.getState());
			if (!siegeEngine.canLoadFuel()) {
				return false;
			}
			if (!GeneralUtil.hasItem((Inventory) state.getInventory(), stack)) {
				return false;
			}
			state.getInventory().removeItem(stack);
			siegeEngine.getAmmoHolder().setLoadedFuel(siegeEngine.getAmmoHolder().getLoadedFuel() + 1);
			return true;
		}
		return false;
	}

	public static void sendPropellantStatusMSG(Player player, SiegeEngine siegeEngine) {
		player.sendMessage("§eReloaded propellant");
		if (!siegeEngine.canLoadFuel()) {
			player.sendMessage("§ePropellant is Full! Level: §6(" + siegeEngine.getAmmoHolder().getLoadedFuel() + "/"
					+ siegeEngine.getMaxFuel() + ")");
		} else {
			player.sendMessage(
					"§ePropellant level is: §6(" + siegeEngine.getAmmoHolder().getLoadedFuel() + "/" + siegeEngine.getMaxFuel() + ")");
		}
	}

	public static void sendSiegeEngineHelpMSG(Player player, SiegeEngine siegeEngine) {
		if (!siegeEngine.canLoadFuel()) {
			player.sendMessage("§ePropellant is Full! Level: §6(" + siegeEngine.getAmmoHolder().getLoadedFuel() + "/"
					+ siegeEngine.getMaxFuel() + ")");
			if (siegeEngine.getAmmoHolder().getLoadedProjectile() == 0) {
				player.sendMessage("§eLoad Ammunition by Right Clicking the Siege Engine with any Valid Projectilie!");
			}
		} else {
			player.sendMessage(
					"§ePropellant level is: §6(" + siegeEngine.getAmmoHolder().getLoadedFuel() + "/" + siegeEngine.getMaxFuel() + ")");
		}
	}
	
	public static boolean autoReload(Player player) {
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
					if (siegeEngine == null || !siegeEngine.getEnabled()) {
						continue;
					}
					if (siegeEngine.hasAmmunition()) {
						continue;
					}
					// Checks for ammo in nearby container and loads if found
					if (SiegeEnginesUtil.pulledAmmoFromContainer(siegeEngine.getEntity().getLocation(), siegeEngine)) {
						player.sendMessage("§eReloaded ammunition!");
						continue;
					}
					if (SiegeEnginesUtil.pulledAmmoFromContainer(
							siegeEngine.getEntity().getLocation().getBlock().getRelative(0, -1, 0).getLocation(),
							siegeEngine)) {
						player.sendMessage("§eReloaded ammunition!");
						continue;
					}
					if (SiegeEnginesUtil.pulledAmmoFromContainer(
							siegeEngine.getEntity().getLocation().getBlock().getRelative(0, 1, 0).getLocation(),
							siegeEngine)) {
						player.sendMessage("§eReloaded ammunition!");
						continue;
					}
					// Search player's inv for ammo because it was not found in a nearby container
					if (SiegeEnginesUtil.pulledAmmoFromPlayer(player, siegeEngine)) {
						player.sendMessage("§eReloaded ammunition!");
						continue;
					}
					if (!siegeEngine.hasAmmunition()) {
						player.sendMessage("§eCould not reload ammunition.");
					}
					// Old Non-Firework Compatible
					// event.getPlayer().getInventory().getContents()
					for (ItemStack inventoryItem : player.getInventory().getContents()) {
						for (ItemStack stack : siegeEngine.getProjectiles().keySet()) {
							if (siegeEngine.hasAmmunition()) {
								continue;
							}
							if (!GeneralUtil.hasItem(player.getInventory(), stack))
								continue;
							if (stack.isSimilar(inventoryItem) && siegeEngine.getAmmoHolder().getLoadedProjectile() == 0) {
								siegeEngine.getAmmoHolder().setLoadedProjectile(1);
								siegeEngine.getAmmoHolder().setMaterialName(stack);
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
					if (siegeEngine == null || !siegeEngine.getEnabled()) {
						continue;
					}
					// Checks for propellant in nearby container and loads if found
					if (SiegeEnginesUtil.pulledPropellantFromContainer(siegeEngine.getEntity().getLocation(), siegeEngine)) {
						SiegeEnginesUtil.sendPropellantStatusMSG(player, siegeEngine);
						continue;
					}
					if (SiegeEnginesUtil.pulledPropellantFromContainer(
							siegeEngine.getEntity().getLocation().getBlock().getRelative(0, -1, 0).getLocation(),
							siegeEngine)) {
						SiegeEnginesUtil.sendPropellantStatusMSG(player, siegeEngine);
						continue;
					}
					if (SiegeEnginesUtil.pulledPropellantFromContainer(
							siegeEngine.getEntity().getLocation().getBlock().getRelative(0, 1, 0).getLocation(),
							siegeEngine)) {
						SiegeEnginesUtil.sendPropellantStatusMSG(player, siegeEngine);
						continue;
					}
					// Check player's inv for propellant
					if (siegeEngine.canLoadFuel()) {
						ItemStack stack = siegeEngine.getFuelItem();
						if (GeneralUtil.hasItem((Inventory) player.getInventory(), stack)) {
							siegeEngine.getAmmoHolder().setLoadedFuel(siegeEngine.getAmmoHolder().getLoadedFuel() + 1);
							stack.setAmount(1);
							player.getInventory().removeItem(stack);
							SiegeEnginesUtil.sendPropellantStatusMSG(player, siegeEngine);
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
	
	public static void saveSiegeEngine(Player player, Block block) {
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
			SiegeEnginesUtil.doAimUp(ent, 0, player);
		}
	}

	
}
