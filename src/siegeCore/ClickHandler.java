package siegeCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;

import CrunchProjectiles.ExplosiveProjectile;

public class ClickHandler implements Listener {

	public float MinDelay = 5;

	public static HashMap<UUID, ExplosiveProjectile> projectiles = new HashMap<UUID, ExplosiveProjectile>();

	@EventHandler
	public void onHit(ProjectileHitEvent event) {
		if ((event.getEntity() instanceof Snowball) && projectiles.containsKey(event.getEntity().getUniqueId())) {
			ExplosiveProjectile proj = projectiles.get(event.getEntity().getUniqueId());
			Entity snowball = event.getEntity();
			Location loc = snowball.getLocation();
			World world = event.getEntity().getWorld();
			//	world.createExplosion(loc, proj.Radius, proj.DoFire);
			Entity tnt = event.getEntity().getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
			TNTPrimed tntEnt = (TNTPrimed) tnt;
			tntEnt.setYield(proj.Radius);
			tntEnt.setFuseTicks(0);



			projectiles.remove(event.getEntity().getUniqueId());
		}
	}


	public void Shoot(Player player, long delay) {
		float actualDelay = delay;
		Boolean FirstShot = true;
		for (Entity ent : CrunchSiegeCore.TrackedStands.get(player.getUniqueId())){{

			if (ent == null || ent.isDead()) {
				continue;
			}
			double distance = player.getLocation().distance(ent.getLocation());
			if (distance >= 250) {
				player.sendMessage("Too far away to fire");
				return;
			}
			SiegeEquipment siege = CrunchSiegeCore.equipment.get(ent.getUniqueId());

			if (siege.isLoaded()) {
				siege.Fire(player, actualDelay);
				actualDelay += delay;
			}
			else {
				player.sendMessage("Cannon is not loaded");
			}
			//	player.sendMessage(String.format("§e" +actualDelay));

		}
		}
	}


	@EventHandler
	public void rightClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		ItemStack ItemInHand = event.getPlayer().getInventory().getItemInMainHand();
		if (ItemInHand == null) {
			return;
		}

		if (ItemInHand.getType() == Material.PAPER) {
			ItemMeta meta = ItemInHand.getItemMeta();
			if (meta.hasCustomModelData() && meta.getCustomModelData() == 505050505) {
				CrunchSiegeCore.CreateTrebuchet(player);
				ItemInHand.setAmount(ItemInHand.getAmount() - 1);
				return;
			}
		}


		if (event.getAction() == Action.LEFT_CLICK_AIR) {
			if (ItemInHand.getType() != Material.CLOCK) {
				return;
			}

			Shoot(player, 6);

		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onSignChange(SignChangeEvent event){
		String topline = event.getLine(0);
		if (topline == null) topline = "";
		Player player = event.getPlayer();
		String toplinetrimmed = topline.trim();
		event.getPlayer().sendMessage("1");
		if (toplinetrimmed.equals("[Cannon]")) {
			SaveCannons(player, event.getBlock());
		}
		event.getPlayer().sendMessage("2");
	}



	public static void TakeControl(Player player, Entity entity) {
		LivingEntity living = (LivingEntity) entity;
		if (CrunchSiegeCore.TrackedStands.containsKey(player.getUniqueId())) {
			List<Entity> entities = CrunchSiegeCore.TrackedStands.get(player.getUniqueId());
			if (entities.contains(entity)) {
				return;
			}
		}

		if (living.getEquipment().getHelmet() != null && living.getEquipment().getHelmet().getType() == Material.CARVED_PUMPKIN) {
			if (living.getEquipment() == null || living.getEquipment().getHelmet() == null || living.getEquipment().getHelmet().getItemMeta() == null) {
				return;
			}

			ArmorStand stand = (ArmorStand) entity;
			stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
			SiegeEquipment equip;
			if (CrunchSiegeCore.equipment.containsKey(entity.getUniqueId())) {
				equip = CrunchSiegeCore.equipment.get(entity.getUniqueId());
			}
			else {
				equip = CrunchSiegeCore.CreateClone(living.getEquipment().getHelmet().getItemMeta().getCustomModelData());
			
				equip.AmmoHolder = new EquipmentMagazine();
				equip.Entity = entity;
				equip.EntityId = entity.getUniqueId();
			}

			if (CrunchSiegeCore.TrackedStands.containsKey(player.getUniqueId())) {
				List<Entity> entities = CrunchSiegeCore.TrackedStands.get(player.getUniqueId());
				entities.add(entity);
				CrunchSiegeCore.TrackedStands.put(player.getUniqueId(), entities);
			}
			else {
				List<Entity> newList = new ArrayList<Entity>();
				newList.add(entity);
				CrunchSiegeCore.TrackedStands.put(player.getUniqueId(), newList);
			}
			CrunchSiegeCore.equipment.put(entity.getUniqueId(), equip);
			player.sendMessage("§eNow controlling the equipment.");
		}
	}

	public void SaveCannons(Player player, Block block) {
		List<String> Ids = new ArrayList<String>();
		if (!CrunchSiegeCore.TrackedStands.containsKey(player.getUniqueId())) {
			return;
		}
		for (Entity ent : CrunchSiegeCore.TrackedStands.get(player.getUniqueId())) {

			Ids.add(ent.getUniqueId().toString());
		}
		TileState state = (TileState) block.getState();
		NamespacedKey key = new NamespacedKey(CrunchSiegeCore.plugin, "cannons");		
		state.getPersistentDataContainer().set(key, PersistentDataType.STRING, String.join(",", Ids));
		state.update();
		player.sendMessage("Saving cannons!");
	}

	public void AimUp(Player player, float amount) {

		for (Entity ent : CrunchSiegeCore.TrackedStands.get(player.getUniqueId())) {
			Location loc = ent.getLocation();
			ArmorStand stand = (ArmorStand) ent;
			//	player.sendMessage(String.format("" + loc.getPitch()));
			if (loc.getPitch() == -85 || loc.getPitch() - amount < -85) {
				return;
			}
			loc.setPitch((float) (loc.getPitch() - amount));
			stand.setHeadPose(new EulerAngle(loc.getDirection().getY()*(-1),0,0));


			ent.teleport(loc);
		}
	}

	public void AimDown(Player player, float amount) {

		for (Entity ent : CrunchSiegeCore.TrackedStands.get(player.getUniqueId())) {
			Location loc = ent.getLocation();
			ArmorStand stand = (ArmorStand) ent;
			//	player.sendMessage(String.format("" + loc.getPitch()));
			if (loc.getPitch() == 85 || loc.getPitch() + amount > 85) {
				return;
			}

			loc.setPitch((float) (loc.getPitch() + amount));
			stand.setHeadPose(new EulerAngle(loc.getDirection().getY()*(-1),0,0));
			ent.teleport(loc);
		}
	}

	@EventHandler
	public void onPlayerClickSign(PlayerInteractEvent event){
		Player player = event.getPlayer();
		if(event.getClickedBlock() != null && event.getClickedBlock().getType().toString().contains("SIGN")){
			Sign sign = (Sign) event.getClickedBlock().getState();
			if(sign.getLine(0).equalsIgnoreCase( "[Fire]") && event.getAction() == Action.RIGHT_CLICK_BLOCK){
				if (!CrunchSiegeCore.TrackedStands.containsKey(player.getUniqueId())) {
					return;
				}
				try {
					Long delay = Long.parseLong(sign.getLine(1));
					if (delay < 6){
						delay = 6l;
					}
					Shoot(player, delay);
				} catch (Exception e) {
					Shoot(player, 6);
				}
				return;
			}

			if(sign.getLine(0).equalsIgnoreCase( "[Aim]")){
				if (!CrunchSiegeCore.TrackedStands.containsKey(player.getUniqueId())) {
					return;
				}
				float amount;
				try {
					amount = Float.parseFloat(sign.getLine(1));
					if (player.isSneaking()) {
						AimDown(player, amount);
					}
					else {
						AimUp(player, amount);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					player.sendMessage("Could not parse number on second line.");
				} 

				return;
			}

			if(sign.getLine(0).equalsIgnoreCase( "[Cannon]")){
				if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
					CrunchSiegeCore.TrackedStands.remove(player.getUniqueId());
					player.sendMessage("Releasing the cannons!");
					return;
				}
				
				if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
					if (player.isSneaking()) {
						SaveCannons(player, event.getClickedBlock());
						return;
					}
				
					NamespacedKey key = new NamespacedKey(CrunchSiegeCore.plugin, "cannons");		
					TileState state = (TileState)  sign.getBlock().getState();
					CrunchSiegeCore.TrackedStands.remove(player.getUniqueId());
					List<UUID> temp = new ArrayList<UUID>();
					if (!state.getPersistentDataContainer().has(key,  PersistentDataType.STRING)) {
						return;
					}
					String[] split = state.getPersistentDataContainer().get(key, PersistentDataType.STRING).replace("[", "").replace("]", "").split(",");
					for (String s : split) {
						player.sendMessage(s.trim());
						temp.add(UUID.fromString(s.trim()));
					}
					for (UUID Id : temp) {
						List<Entity> entities = new ArrayList<Entity>();
						Entity ent = Bukkit.getEntity(Id);
						if (ent != null) {
							TakeControl(player, ent);
						}
					}
				}
			}

		}
	}

	@EventHandler
	public void onEntityClick(PlayerInteractAtEntityEvent event) {

		Player player = event.getPlayer();
		ItemStack itemInHand = player.getInventory().getItemInMainHand();
		Entity entity = event.getRightClicked();
		if (entity == null) {
			return;
		}
		if (entity.getType() == EntityType.ARMOR_STAND){
			TakeControl(player, entity);
			if (CrunchSiegeCore.equipment.containsKey(entity.getUniqueId())) {
				SiegeEquipment equipment = CrunchSiegeCore.equipment.get(entity.getUniqueId());
				if (itemInHand.getType().equals(equipment.FuelMaterial)) {
					if (equipment.LoadFuel(player)) {
						player.sendMessage("Loaded " + equipment.AmmoHolder.LoadedFuel + "/" + equipment.MaxFuel);
					}
					else {
						player.sendMessage("Could not load powder.");
					}
				}
				if (itemInHand.getType().equals(Material.FLINT)) {
					if (equipment.isLoaded()) {
						equipment.Fire(player, 6);
					}
					else {
						player.sendMessage("Cannon is not loaded");
					}
					return;
				}

				if (equipment.Projectiles.containsKey(itemInHand.getType()) && equipment.AmmoHolder.LoadedProjectile == 0){
					equipment.AmmoHolder.LoadedProjectile = 1;
					equipment.AmmoHolder.MaterialName = (Material) itemInHand.getType();
					player.sendMessage("Adding projectile to cannon");
					itemInHand.setAmount(itemInHand.getAmount() - 1);
				}
				return;
			}
		}
	}
}
