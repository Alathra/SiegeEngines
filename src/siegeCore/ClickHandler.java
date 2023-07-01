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
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
	
	public void Search(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Collection<Entity> yeet = player.getLocation().getWorld().getNearbyEntities(player.getLocation(), 10, 10, 10);
		for (Entity entity : yeet ) {
			if (CrunchSiegeCore.equipment.containsKey(entity.getUniqueId())){
				player.sendMessage("§eNow controlling the equipment.");
				return;
			}
		}

		for (Entity entity : yeet ) {
			if (entity.getCustomName() == "Trebuchet" && entity.getType() == EntityType.ARMOR_STAND){
				LivingEntity living = (LivingEntity) entity;
				if (living.getEquipment().getHelmet() != null && living.getEquipment().getHelmet().getType() == Material.CARVED_PUMPKIN) {

					SiegeEquipment equip = new SiegeEquipment(entity.getUniqueId());
					ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
					equip.ReadyModelNumber = 122;
					equip.ModelNumberToFireAt = 135;
					equip.MillisecondsBetweenFiringStages = 2;
					equip.MillisecondsBetweenReloadingStages = 50;
					equip.FiringModelNumbers = new ArrayList<>(Arrays.asList(
							123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139
							));
					equip.Entity = entity;
					entity.setCustomName("Trebuchet");
					ItemMeta meta = item.getItemMeta();
					meta.setCustomModelData(equip.ReadyModelNumber);
					item.setItemMeta(meta);


					ArmorStand stand = (ArmorStand) entity;
					stand.setSmall(true);
					((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 2000000, 1));
					living.getEquipment().setHelmet(item);
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
				}

				return;
			}
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
		
		if (ItemInHand.getType() == Material.CLOCK && player.isSneaking()) {
			Search(event);
		}

		if (event.getAction() == Action.LEFT_CLICK_AIR) {
			if (ItemInHand.getType() != Material.STICK) {
				return;
			}
				
			Shoot(player);
	
			}
		}
	


	@EventHandler
	public void onEntityClick(PlayerInteractEntityEvent event) {

		Player player = event.getPlayer();
		if (event.getRightClicked() instanceof ArmorStand) {
			ItemStack itemInHand = player.getInventory().getItemInMainHand();

			if (itemInHand == null) {
				return;
			}
			if (itemInHand.getType() == Material.STICK) {


			}

		}
	}
}
