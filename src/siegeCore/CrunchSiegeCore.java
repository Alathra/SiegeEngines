package siegeCore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;

public class CrunchSiegeCore extends JavaPlugin {
	public static Plugin plugin;

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		plugin = this;

		this.getCommand("siegetest").setExecutor(new SiegeCommand());
		getServer().getPluginManager().registerEvents(new PlayerMoving(), this);
	}

	public class SiegeCommand implements CommandExecutor {

		// This method is called, when somebody uses our command
		@Override
		@EventHandler
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				Location l = player.getLocation();
				Entity entity2 = player.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
				entity2.setInvulnerable(true);
				SiegeEquipment equip = new SiegeEquipment(entity2.getUniqueId());
				ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
				equip.ReadyModelNumber = 122;
				equip.ModelNumberToFireAt = 135;
				equip.MillisecondsBetweenFiringStages = 2;
				equip.MillisecondsBetweenReloadingStages = 50;
				equip.FiringModelNumbers = new ArrayList<>(Arrays.asList(
					123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139
				));
				ItemMeta meta = item.getItemMeta();
				meta.setCustomModelData(equip.ReadyModelNumber);
				item.setItemMeta(meta);
				
				LivingEntity ent = (LivingEntity) entity2;
				ArmorStand stand = (ArmorStand) ent;
				stand.setVisible(false);
				ent.getEquipment().setHelmet(item);
				TrackedStands.put(player.getUniqueId(), entity2.getUniqueId());
				equipment.put(entity2.getUniqueId(), equip);
				
			}
			return true;
		}
	}

	public HashMap<UUID, UUID> TrackedStands = new HashMap<UUID, UUID>();

	private double getPlayerFacingPI(Player p) {

		double deg = 0;
		double fin = 0;
		double x = p.getLocation().getDirection().getX();
		double z = p.getLocation().getDirection().getZ();

		if(x > 0.0 && z >0.0) {
			deg = x;
			fin = (Math.PI/2)*deg;
		}
		if(x> 0.0 && z<0.0) {
			deg = z*(-1);
			fin = ((Math.PI/2)*deg)+(Math.PI/2);
		}
		if(x<0.0 && z<0.0) {
			deg = x*(-1);
			fin = (Math.PI/2)*deg+ Math.PI;
		}
		if(x<0.0 && z>0.0) {
			System.out.println("SW");
			deg = z;
			fin = (Math.PI/2)*deg+(Math.PI/2 *3);
		}
		return fin;
	}

	public HashMap<UUID, SiegeEquipment> equipment = new HashMap<UUID, SiegeEquipment>();

	public HashMap<UUID, SiegeProjectile> projectiles = new HashMap<UUID, SiegeProjectile>();
	public String convertTime(long time){

		long days = TimeUnit.MILLISECONDS.toDays(time);
		time -= TimeUnit.DAYS.toMillis(days);

		long hours = TimeUnit.MILLISECONDS.toHours(time);
		time -= TimeUnit.HOURS.toMillis(hours);

		long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
		time -= TimeUnit.MINUTES.toMillis(minutes);

		long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
		String timeLeftFormatted = String.format("§e" + minutes + " Minutes " +seconds +" Seconds§f");

		return timeLeftFormatted;
	}

	public void UpdateEntityIdModel(UUID entityId, int modelNumber, String WorldName) {
		for (Entity ent : Bukkit.getServer().getWorld(WorldName).getEntities()) {
			if (ent.getUniqueId() == entityId) {
				if (ent instanceof LivingEntity)
				{
					LivingEntity liv = (LivingEntity) ent;
					ItemStack Helmet = liv.getEquipment().getHelmet();
					if (Helmet != null) {
						ItemMeta meta = Helmet.getItemMeta();
						meta.setCustomModelData(modelNumber);
						Helmet.setItemMeta(meta);
						liv.getEquipment().setHelmet(Helmet);
					//	plugin.getLogger().log(Level.INFO, "Updating stand?");
					}
				}
			}
		}
	}
	

	public class PlayerMoving implements Listener {
		@EventHandler
		public void playerMove(PlayerMoveEvent event) {
			Player player = event.getPlayer();

			if (TrackedStands.containsKey(player.getUniqueId())) {
				ItemStack itemInHand = player.getInventory().getItemInMainHand();
				if (itemInHand != null) {
					if (itemInHand.getType() != Material.STICK) {
						//	TrackedStands.remove(player.getUniqueId());

						return;
					}
					UUID trackedId = TrackedStands.get(player.getUniqueId());
					for (Entity ent : player.getWorld().getEntities()) {
						if (ent.getUniqueId() == trackedId) {
							//	player.sendMessage("got id");
							LivingEntity living = (LivingEntity) ent;
							Location loc = ent.getLocation();
							loc.setDirection(player.getLocation().getDirection());
							loc.setYaw(player.getLocation().getYaw());
							loc.setPitch(player.getLocation().getPitch());
							ArmorStand stand = (ArmorStand) living;

							living.teleport(loc);
						}
					}
				}
			}

		}


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
		@EventHandler
		public void rightClick(PlayerInteractEvent event) {
			Player player = event.getPlayer();

			ItemStack ItemInHand = event.getPlayer().getInventory().getItemInMainHand();
			if (ItemInHand == null) {
				return;
			}
			UUID trackedId = TrackedStands.get(player.getUniqueId());
			if (ItemInHand.getType() == Material.CLOCK) {
				if (equipment.containsKey(trackedId)) {
					SiegeEquipment siege = equipment.get(trackedId);
					if (event.getAction() ==  Action.LEFT_CLICK_AIR) {
						siege.FuseTime += 1;
						player.sendMessage("§c Fuse Time set to " + siege.FuseTime);
						return;
					}
					if (event.getAction() ==  Action.RIGHT_CLICK_AIR) {
						siege.FuseTime -= 1;
						if (siege.FuseTime < 10) {
							siege.FuseTime = 10;
						}
						player.sendMessage("§cFuse Time set to " + siege.FuseTime);
						return;
					}
				}
			}

			if (ItemInHand.getType() == Material.BOOK) {
				if (equipment.containsKey(trackedId)) {
					SiegeEquipment siege = equipment.get(trackedId);
					if (event.getAction() ==  Action.LEFT_CLICK_AIR) {
						siege.Velocity += 0.1;
						player.sendMessage("§eProjectile Speed set to " + siege.Velocity);
						return;
					}
					if (event.getAction() ==  Action.RIGHT_CLICK_AIR) {
						siege.Velocity -= 0.1;
						player.sendMessage("§eProjectile Speed set to " + siege.Velocity);
						return;
					}
				}
			}

			if (event.getAction() == Action.LEFT_CLICK_AIR) {
				if (ItemInHand.getType() != Material.BLAZE_ROD) {
					return;
				}

				for (Entity ent : player.getWorld().getEntities()) {
					if (ent.getUniqueId() == trackedId) {
						SiegeEquipment siege = equipment.get(trackedId);

						if (System.currentTimeMillis() < siege.NextShotTime) {
							player.sendMessage("Cannot fire for another " + convertTime(siege.NextShotTime - System.currentTimeMillis()));
							return;
						}

						LivingEntity living = (LivingEntity) ent;
						Location loc = living.getEyeLocation();
						loc.setPitch(-30);
						loc.setY(loc.getY() + 2);
						loc.getPitch();

						Random random = new Random();

						float randomVar = random.nextFloat() * (7 - -7) + -7;
						loc.setYaw(loc.getYaw() + randomVar);
						siege.NextModelNumber = 0;
					    siege.location = loc;
						siege.NextShotTime = System.currentTimeMillis() + 60000;
						player.sendMessage("Cannot fire for another " + convertTime(siege.NextShotTime - System.currentTimeMillis()));
						siege.WorldName = ent.getWorld().getName();
						Bukkit.getServer().getWorld(siege.WorldName).playSound(siege.location, Sound.ENTITY_BAT_DEATH, 20, 2);
						siege.TaskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
							//player.sendMessage("task");
							if (siege.HasFired) {
								Bukkit.getServer().getScheduler().cancelTask(siege.TaskNumber);
								siege.TaskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
								//	player.sendMessage("task");
									if (siege.HasReloaded) {
										Bukkit.getServer().getScheduler().cancelTask(siege.TaskNumber);
										siege.HasReloaded = false;
										siege.HasFired = false;
										siege.NextModelNumber = 0;
									}
									else {
										//firing stages
										if (siege.NextModelNumber - 1 <= siege.FiringModelNumbers.size() && siege.NextModelNumber - 1 >= 0) {
											int modelData = siege.FiringModelNumbers.get(siege.NextModelNumber - 1);
										//	player.sendMessage("" + modelData);
											UpdateEntityIdModel(siege.EntityId, modelData, siege.WorldName);
											siege.NextModelNumber -= 1;
											
										}else {
										//	plugin.getLogger().log(Level.INFO, "its reloaded");
											siege.HasReloaded = true;
										}
									}
								}, 0, siege.MillisecondsBetweenReloadingStages);
						
							}
							else {
								//firing stages
								if (siege.NextModelNumber < siege.FiringModelNumbers.size()) {
							
									int modelData = siege.FiringModelNumbers.get(siege.NextModelNumber);
								//	player.sendMessage("" + modelData);
									UpdateEntityIdModel(siege.EntityId, modelData, siege.WorldName);
									if (modelData == siege.ModelNumberToFireAt) {
										Entity tnt = Bukkit.getServer().getWorld(siege.WorldName).spawnEntity(loc, EntityType.SNOWBALL);
										
										projectiles.put(tnt.getUniqueId(), siege.projectile);
										tnt.setVelocity(loc.getDirection().multiply(siege.Velocity));
									
									}
									siege.NextModelNumber += 1;
									
								}else {
									siege.HasFired = true;
							
								}
							}
						}, 0, siege.MillisecondsBetweenFiringStages);
						return;

					}
				}

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

}
