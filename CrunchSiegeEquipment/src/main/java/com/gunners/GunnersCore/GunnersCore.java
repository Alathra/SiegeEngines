package com.gunners.GunnersCore;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
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
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
//import com.palmergames.bukkit.towny.Towny;

//import GunnersProjectiles.EntityProjectile;
//import GunnersProjectiles.GunnersProjectile;
//import GunnersProjectiles.ExplosiveProjectile;
//import GunnersProjectiles.PotionProjectile;

public class GunnersCore extends JavaPlugin {
	public static Plugin plugin;

	public static Random random = new Random();
	private static String Path;

	//public static Towny towny;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onDisable() {
		for (GunnerEquipment equip : equipment.values()) {
			if (equip.Entity != null) {
				ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
				ItemMeta meta = item.getItemMeta();
				equip.AmmoHolder = new EquipmentMagazine();
				
				meta.setCustomModelData(equip.ReadyModelNumber);
				meta.setDisplayName("§e" + equip.EquipmentName +" Item");
				List<String> Lore = new ArrayList<String>();
				Lore.add("§ePlace as a block to spawn a " + equip.EquipmentName + " or put on an Armor Stand.");
				meta.setLore(Lore);
				item.setItemMeta(meta);

				((LivingEntity) equip.Entity).getEquipment().setHelmet(item);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		plugin = this;
		Path = this.getDataFolder().getAbsolutePath();
		this.getCommand("gunnerscore").setExecutor(new GunnersCommand());
		getServer().getPluginManager().registerEvents(new RotationHandler(), this);
		getServer().getPluginManager().registerEvents(new ClickHandler(), this);
		//StorageManager.setup(Path, plugin);
		equipment.clear();
		TrackedStands.clear();
		DefinedEquipment.clear();
		AddDefaults();
		HashMap<ItemStack,GunnersProjectile> projObj = new HashMap<>();
		GunnerEquipment equip = CreateNewGun(null, null, null, null, null, null, projObj);
		for (GunnerEquipment i : DefinedEquipment.values()) {
			System.out.println("§eEnabled Weapon : "+i.EquipmentName);
			System.out.println("§eWeapon Propellant/\"Fuel\" ItemStacks : "+i.FuelMaterial);
			for (ItemStack proj : i.Projectiles.keySet()) {
				System.out.println("§eWeapon Projectile ItemStacks : "+proj);
			}
		}
	}

	public class GunnersCommand implements CommandExecutor {
		@Override
		@EventHandler
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			Player player = null;
			if (sender instanceof Player) {
				player = (Player) sender;
			}
			if (args != null && args.length > 0) {

				switch (args[0].toLowerCase()) {
				case "get":
					if (player == null) {
						sender.sendMessage("§eonly a player can use this command.");
						return true;	
					}
					if (!sender.hasPermission("gunnerscore.get")) {
						sender.sendMessage("§eNo perms to gunnerscore.get");
						return true;
					}
					for (GunnerEquipment i : DefinedEquipment.values()) {
						ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
						ItemMeta meta = item.getItemMeta();
						meta.setCustomModelData(i.ReadyModelNumber);
						meta.setDisplayName("§e" + i.EquipmentName +" Item");
						List<String> Lore = new ArrayList<String>();
						Lore.add("§ePlace as a block to spawn a " + i.EquipmentName);
						Lore.add("§eor put on an Armor Stand.");
						Lore.add("§eRight click to toggle visibility of stand.");
						meta.setLore(Lore);
						item.setItemMeta(meta);
						player.getInventory().addItem(item);
					}
					break;
				case "reload":
					if (!sender.hasPermission("gunnerscore.reload")) {
						sender.sendMessage("§eNo perms to gunnerscore.reload");
						return true;
					}
					equipment.clear();
					TrackedStands.clear();
					DefinedEquipment.clear();
					AddDefaults();
					HashMap<ItemStack,GunnersProjectile> projObj = new HashMap<>();
					GunnerEquipment equip = CreateNewGun(null, null, null, null, null, null, projObj);
					for (GunnerEquipment i : DefinedEquipment.values()) {
						sender.sendMessage("§eEnabled Weapon : "+i.EquipmentName);
						sender.sendMessage("§eWeapon Propellant/\"Fuel\" ItemStacks : "+i.FuelMaterial);
						for (ItemStack proj : i.Projectiles.keySet()) {
							sender.sendMessage("§eWeapon Projectile ItemStacks : "+proj);
						}
					}
					sender.sendMessage("§eGunners Core configs reloaded");
					break;
				}

			}
			else {
				sender.sendMessage("§eIncorrect usage, /gunnerscore get, /gunnerscore reload");
			}
			return true;
		}
	}

	public static GunnerEquipment CreateClone(Integer ModelId) {
		try {
			return DefinedEquipment.get(ModelId).clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block

		}
		return null;
	}

	public static HashMap<Integer, GunnerEquipment> DefinedEquipment = new HashMap<Integer, GunnerEquipment>();

	public static HashMap<UUID, List<Entity>> TrackedStands = new HashMap<UUID, List<Entity>>();

	public static HashMap<UUID, GunnerEquipment> equipment = new HashMap<UUID, GunnerEquipment>();

	public static String convertTime(long time){

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
	
	public static GunnerEquipment CreateNewGun(String name, Integer XOffset, Integer YOffset, Integer fuelMax, Float fuelVelocityMod, Integer customModelId, HashMap<ItemStack,GunnersProjectile> projObj) {
		GunnerEquipment equip = new GunnerEquipment();
		if (customModelId == null || customModelId == 0)
			customModelId = 150;
		if (name == null)
			name = "Peckle Gun";
		if (XOffset == null)
			XOffset = 0;
		if (YOffset == null)
			YOffset = 0;
		if (fuelMax == null)
			fuelMax = 0;
		if (fuelVelocityMod == null)
			fuelVelocityMod = 0.95f;
		equip.EquipmentName = name;
		equip.XOffset = XOffset;
		equip.YOffset = YOffset;
		equip.RotateStandHead = true;
		equip.RotateSideways = true;
		equip.PlacementOffsetY = -1.125f;
		equip.ReadyModelNumber = customModelId;
		equip.ModelNumberToFireAt = customModelId;
		equip.FiringModelNumbers = new ArrayList<Integer>();
		equip.VelocityPerFuel = fuelVelocityMod;
		equip.FuelMaterial = new ItemStack(Material.GUNPOWDER);
		equip.CycleThroughModelsBeforeFiring = false;
		equip.MaxFuel = fuelMax;
		equip.shotAmount = 1;
		equip.AmmoHolder.LoadedFuel = fuelMax;
		EntityProjectile defaultProj = new EntityProjectile();
		defaultProj.EntityCount = 6;
		defaultProj.EntityTyp = EntityType.SHULKER_BULLET;
		defaultProj.ParticleType = Particle.WHITE_ASH;
		defaultProj.SoundType = Sound.ENTITY_BLAZE_SHOOT;
		if (projObj.keySet().isEmpty() || projObj.isEmpty())
			projObj.put(new ItemStack(Material.GUNPOWDER),defaultProj);
		for (ItemStack mat : projObj.keySet()) {
			equip.Projectiles.put(mat,projObj.get(mat));
		}
		DefinedEquipment.put(equip.ReadyModelNumber,equip);
		return equip;
	}

	public static void AddDefaults() {
		GunnerEquipment equip = new GunnerEquipment();
		equip.EquipmentName = "Trebuchet";
		equip.XOffset = 5;
		equip.YOffset = 5;
		equip.VelocityPerFuel = 0.3f;
		ExplosiveProjectile proj = new ExplosiveProjectile();
		proj.ExplodePower = 2;
		equip.shotAmount = 1;
		equip.RotateStandHead = false;
		equip.RotateSideways = true;
		equip.FuelMaterial = new ItemStack(Material.STRING);
		equip.Projectiles.put(new ItemStack(Material.COBBLESTONE), proj);
		equip.ReadyModelNumber = 122;
		equip.ModelNumberToFireAt = 135;
		equip.MillisecondsBetweenFiringStages = 2;
		equip.MillisecondsBetweenReloadingStages = 10;
		equip.FiringModelNumbers = new ArrayList<>(Arrays.asList(
				123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139
				));
		equip.CycleThroughModelsBeforeFiring = true;
		DefinedEquipment.put(equip.ReadyModelNumber, equip);
		
		equip = new GunnerEquipment();
		equip.EquipmentName = "Naval Cannon";
		equip.Projectiles.put(new ItemStack(Material.COBBLESTONE), proj);
		equip.PlacementOffsetY = -1;
		equip.shotAmount = 1;
		equip.ReadyModelNumber = 142;
		equip.ModelNumberToFireAt = 142;
		equip.FiringModelNumbers = new ArrayList<Integer>();
		equip.RotateStandHead = true;
		equip.RotateSideways = false;
		proj = new ExplosiveProjectile();
		proj.ExplodePower = 1;
		proj.ProjectilesCount = 3;
		proj.DelayedFire = true;
		proj.Inaccuracy = 0.75f;
		equip.Projectiles.put(new ItemStack(Material.TNT), proj);
		proj = new ExplosiveProjectile();
		proj.ExplodePower = 4;
		proj.ProjectilesCount = 1;
		equip.Projectiles.put(new ItemStack(Material.COPPER_BLOCK), proj);

		EntityProjectile fireProj = new EntityProjectile();
		fireProj.EntityCount = 2;
		fireProj.EntityTyp = EntityType.SMALL_FIREBALL;
		fireProj.ParticleType = Particle.WHITE_ASH;
		fireProj.SoundType = Sound.ENTITY_BLAZE_SHOOT;
		equip.Projectiles.put(new ItemStack(Material.FIRE_CHARGE), fireProj);
		DefinedEquipment.put(equip.ReadyModelNumber, equip);
		equip = new GunnerEquipment();
		equip.EquipmentName = "Siege Cannon";
		proj.ExplodePower = 2;
		equip.Projectiles.put(new ItemStack(Material.COBBLESTONE), proj);
		equip.shotAmount = 1;
		equip.PlacementOffsetY = -1;
		equip.ReadyModelNumber = 141;
		equip.ModelNumberToFireAt = 141;
		equip.FiringModelNumbers = new ArrayList<Integer>();
		equip.RotateStandHead = true;
		equip.RotateSideways = true;
		proj = new ExplosiveProjectile();
		proj.ExplodePower = 1;
		proj.ProjectilesCount = 3;
		proj.DelayedFire = true;
		proj.Inaccuracy = 0.5f;
		equip.Projectiles.put(new ItemStack(Material.TNT), proj);
		proj = new ExplosiveProjectile();
		proj.ExplodePower = 4;
		proj.ProjectilesCount = 1;
		equip.Projectiles.put(new ItemStack(Material.COPPER_BLOCK), proj);
		equip.Projectiles.put(new ItemStack(Material.GRAVEL), new EntityProjectile());
		DefinedEquipment.put(equip.ReadyModelNumber, equip);
//		EntityProjectile pig = new EntityProjectile();
//		pig.EntityCount = 100;
//		pig.DelayedFire = true;
//		pig.DelayTime = 3;
//		pig.EntityTyp = EntityType.ARROW;
//		pig.SoundType = Sound.ITEM_CROSSBOW_SHOOT;
//		pig.ParticleType = Particle.GLOW;
//		equip.Projectiles.put(Material.PORKCHOP, pig);
//	  equip.Projectiles.put(Material.BONE, new PotionProjectile());

	}

	public static Boolean CreateCannon(Entity player, int CustomModelData, Location l) {
		//l.setY(l.getY() - 1);
		l.add(0.5, 0, 0.5);
		NamespacedKey key = new NamespacedKey(GunnersCore.plugin, "cannons");	
		GunnerEquipment equip = CreateClone(CustomModelData);
		if (equip == null || !equip.Enabled) {
			return false;
		}
		l.setY(l.getY() + 1);
		int maxNearby = 0;

		l.setDirection(player.getFacing().getDirection());
		ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
		ItemMeta meta = item.getItemMeta();
		String id = "";
		equip.AmmoHolder = new EquipmentMagazine();
		if (equip.HaseBaseStand) {
	
			Location l2 = l;
			l2.setY(l.getY() + equip.BaseStandOffset);
			Entity entity3 = player.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
	
			LivingEntity ent = (LivingEntity) entity3;
			ArmorStand stand = (ArmorStand) ent;
			id = entity3.getUniqueId().toString();
			meta.setCustomModelData(equip.BaseStandModelNumber);
			stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.REMOVING_OR_CHANGING);
			stand.setBasePlate(false);
			//	stand.setSmall(true);
			item.setItemMeta(meta);
			stand.setInvisible(equip.AllowInvisibleStand);
			ent.getEquipment().setHelmet(item);
			stand.setGravity(false);
			stand.setMarker(true);
		}
		l.setY(l.getY() + equip.PlacementOffsetY);
		Entity entity2 = player.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
		if (id != "") {
			entity2.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
		}
		meta.setCustomModelData(equip.ReadyModelNumber);
		meta.setDisplayName("§e" + equip.EquipmentName +" Item");
		List<String> Lore = new ArrayList<String>();
		Lore.add("§ePlace as a block to spawn a " + equip.EquipmentName + " or put on an Armor Stand.");
		meta.setLore(Lore);
		item.setItemMeta(meta);

	//	entity3.addPassenger(entity2);
		
		LivingEntity ent = (LivingEntity) entity2;
		ArmorStand stand = (ArmorStand) ent;
		equip.Entity = entity2;
		for (Entity le : l.getNearbyEntities(5, 5, 5)) {
			if (maxNearby >= 4) {
				ent.damage(100.0d,player);
				return false;
			}
			if (le instanceof ArmorStand)
				maxNearby++;
		}
		
		equip.EntityId = entity2.getUniqueId();
		stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.REMOVING_OR_CHANGING);
		stand.setInvisible(equip.AllowInvisibleStand);
		stand.setBasePlate(false);
		ent.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 2000000, 1));
		//	stand.setSmall(true);
		
		stand.setGravity(false);
		//stand.setSmall(true);
		ent.getEquipment().setHelmet(item);
		if (TrackedStands.containsKey(player.getUniqueId())) {
			List<Entity> entities = TrackedStands.get(player.getUniqueId());
			entities.add(entity2);
			TrackedStands.put(player.getUniqueId(), entities);
		}
		else {
			List<Entity> newList = new ArrayList<Entity>();
			newList.add(entity2);
			TrackedStands.put(player.getUniqueId(), newList);
		}
		equipment.put(entity2.getUniqueId(), equip);
		return true;
	}

	public static void UpdateEntityIdModel(Entity ent, int modelNumber, String WorldName) {
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



