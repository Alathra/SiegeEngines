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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ClickHandler implements Listener {


	public static HashMap<UUID, SiegeProjectile> projectiles = new HashMap<UUID, SiegeProjectile>();

	@EventHandler
	public void onHit(ProjectileHitEvent event) {
		if ((event.getEntity() instanceof Snowball) && projectiles.containsKey(event.getEntity().getUniqueId())) {
			SiegeProjectile proj = projectiles.get(event.getEntity().getUniqueId());
			Entity snowball = event.getEntity();
			Location loc = snowball.getLocation();
			World world = event.getEntity().getWorld();
			//	world.createExplosion(loc, proj.Radius, proj.DoFire);
			Entity tnt = event.getEntity().getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
			TNTPrimed tntEnt = (TNTPrimed) tnt;
			tntEnt.setFuseTicks(0);
			tntEnt.setYield(proj.Radius);

			projectiles.remove(event.getEntity().getUniqueId());
		}
	}


	public void Shoot(Player player) {
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

			if (System.currentTimeMillis() < siege.NextShotTime) {
				player.sendMessage("Cannot fire for another " + CrunchSiegeCore.convertTime(siege.NextShotTime - System.currentTimeMillis()));
				return;
			}

			if (player.getInventory().containsAtLeast(new ItemStack(Material.COBBLESTONE), 10)) {
				player.getInventory().remove(new ItemStack(Material.COBBLESTONE, 10));
			}
			else {
				player.sendMessage("Cannot fire, missing cobblestone, requires 10 per shot");
				return;
			}
			siege.Fire(player);
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
			if (ItemInHand.getType() != Material.STICK) {
				return;
			}

			Shoot(player);

		}
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onManualLock(SignChangeEvent event){
		String topline = event.getLine(0);
		if (topline == null) topline = "";
		Player player = event.getPlayer();
		String toplinetrimmed = topline.trim();
		event.getPlayer().sendMessage("1");
		if (toplinetrimmed.equals("[Cannon]")) {
			Block block = event.getBlock();
			List<String> Ids = new ArrayList<String>();
			for (Entity ent : CrunchSiegeCore.TrackedStands.get(player.getUniqueId())) {

				Ids.add(ent.getUniqueId().toString());
			}
			TileState state = (TileState) block.getState();
			NamespacedKey key = new NamespacedKey(CrunchSiegeCore.plugin, "cannons");		
			state.getPersistentDataContainer().set(key, PersistentDataType.STRING, String.join(",", Ids));
			state.update();
			event.getPlayer().sendMessage("Saving cannons!");
		}
		event.getPlayer().sendMessage("2");
	}

	public static void TakeControl(Player player, Entity entity) {
		LivingEntity living = (LivingEntity) entity;
		if (living.getEquipment().getHelmet() != null && living.getEquipment().getHelmet().getType() == Material.CARVED_PUMPKIN) {
			if (living.getEquipment() == null || living.getEquipment().getHelmet() == null || living.getEquipment().getHelmet().getItemMeta() == null) {
				return;
			}

			SiegeEquipment equip = CrunchSiegeCore.CreateClone(living.getEquipment().getHelmet().getItemMeta().getCustomModelData());
			equip.Entity = entity;
			equip.EntityId = entity.getUniqueId();

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

	@EventHandler
	public void onPlayerClickSign(PlayerInteractEvent event){
		Player p = event.getPlayer();
		if(event.getClickedBlock() != null && event.getClickedBlock().getType().toString().contains("SIGN")){
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
				Sign sign = (Sign) event.getClickedBlock().getState();
				//^^ .getState(); really important
				if(sign.getLine(0).equalsIgnoreCase( "[Cannon]")){
					NamespacedKey key = new NamespacedKey(CrunchSiegeCore.plugin, "cannons");		
					TileState state = (TileState)  sign.getBlock().getState();

					List<UUID> temp = new ArrayList<UUID>();
					event.getPlayer().sendMessage("1");
					if (!state.getPersistentDataContainer().has(key,  PersistentDataType.STRING)) {
						return;
					}
					event.getPlayer().sendMessage("2");
					String[] split = state.getPersistentDataContainer().get(key, PersistentDataType.STRING).replace("[", "").replace("]", "").split(",");
					for (String s : split) {
						p.sendMessage(s.trim());
						temp.add(UUID.fromString(s.trim()));
					}
					for (UUID Id : temp) {
						List<Entity> entities = new ArrayList<Entity>();
						Entity ent = Bukkit.getEntity(Id);
						if (ent != null) {
							TakeControl(p, ent);
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
		if (itemInHand.getType() == Material.CLOCK) {
			if (entity.getType() == EntityType.ARMOR_STAND){
				TakeControl(player, entity);
			}
			return;
		}
	}
}
