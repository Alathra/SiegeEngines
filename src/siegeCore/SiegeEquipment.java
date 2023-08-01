package siegeCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import CrunchProjectiles.CrunchProjectile;
import CrunchProjectiles.ExplosiveProjectile;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;

public class SiegeEquipment implements Cloneable  {

	@Override
	public SiegeEquipment clone() throws CloneNotSupportedException {
		return (SiegeEquipment)super.clone();
	}
	public Boolean Enabled = true;
	public UUID EntityId;
	public Entity Entity;

	public HashMap<Material, CrunchProjectile> Projectiles = new HashMap<Material, CrunchProjectile>() ;

	public String WorldName;
	public String EquipmentName = "Cannon";
	public int XOffset = 7;
	public int YOffset = 0;
	public int PitchOffset = -90;
	public int MaxFuel = 5;
	public double PlacementOffsetY = 0;
	public float VelocityPerFuel = 0.75f;
	public Material FuelMaterial = Material.GUNPOWDER;
	public long NextShotTime = System.currentTimeMillis();

	public int MillisecondsBetweenFiringStages;
	public int MillisecondsBetweenReloadingStages;

	public int MillisecondsToLoad;
	public int ModelNumberToFireAt;
	public Boolean CycleThroughModelsBeforeFiring = false;
	public Boolean RotateSideways = false;
	public Boolean RotateUpDown = true;
	public Boolean RotateStandHead = true;
	public Boolean HasFired = false;
	public Boolean HasReloaded = false;
	public Boolean AllowInvisibleStand = true;
	public Boolean HaseBaseStand = false;
	public double BaseStandOffset = 0;
	public int BaseStandModelNumber = 147;
	public int TaskNumber;

	public int ReadyModelNumber;

	public int NextModelNumber = 0;

	public List<Integer> FiringModelNumbers = new ArrayList<Integer>();

	public EquipmentMagazine AmmoHolder = new EquipmentMagazine();

	public Boolean isLoaded() {
		return (AmmoHolder.LoadedFuel > 0 && AmmoHolder.LoadedProjectile >= 1);
	}

	public Boolean CanLoadFuel() {
		return AmmoHolder.LoadedFuel < MaxFuel;
	}

	public Boolean LoadFuel(Player player) {
		ItemStack fuelItem = new ItemStack(this.FuelMaterial, 1);
		if (AmmoHolder.LoadedFuel == this.MaxFuel) {
			return true;
		}
		if (player.getInventory().containsAtLeast(fuelItem, 1) || fuelItem.getType() == Material.AIR) {
			if (CanLoadFuel()) {
				AmmoHolder.LoadedFuel += 1;
				SaveState();
				player.getInventory().removeItem(fuelItem);
				player.sendMessage("Loaded " + this.AmmoHolder.LoadedFuel + "/" + this.MaxFuel);
				return true;
			}
			else {
				return false;
			}
		}

		return false;

	}

	public Boolean LoadProjectile(Player player, ItemStack itemInHand) {
		if (itemInHand.getAmount() <= 0) {
			return false;
		}
		if (this.Projectiles.containsKey(itemInHand.getType()) && this.AmmoHolder.LoadedProjectile == 0){
			this.AmmoHolder.LoadedProjectile = 1;
			this.AmmoHolder.MaterialName = (Material) itemInHand.getType();
			player.sendMessage("Adding projectile to cannon");
			itemInHand.setAmount(itemInHand.getAmount() - 1);
			return true;
		}
		return false;
	}
	
	public void SaveState() {

	}


	public Location GetFireLocation(LivingEntity living) {
		Location loc =  living.getEyeLocation();
		Vector direction = living.getLocation().getDirection().multiply(XOffset);
		loc.add(direction);
		if (this.YOffset > 0) {
			loc.setY(loc.getY() + this.YOffset);
		}
		else {
			loc.setY(loc.getY() - this.YOffset);
		}
		loc.setPitch(loc.getPitch() + this.PitchOffset);
		return loc;
	}
	
	public void ShowFireLocation(Player player) {
	    LivingEntity living = (LivingEntity) this.Entity;
	    if (living != null) {
			Location origin = this.GetFireLocation(living);
			Vector direction = origin.getDirection();
			direction.multiply(5 /* the range */);
			Location destination = origin.clone().add(direction);
					
			direction.normalize();
			for (int i = 0; i < 3 /* range */; i+= 1) {
			    Location spawn = origin.add(direction);
			  
				player.spawnParticle(Particle.CLOUD, spawn, 1, 0,0,0,0);
			} 
	    }

	}
	
	public void Fire(Player player, float delay) {
		if (System.currentTimeMillis() < this.NextShotTime) {
			player.sendMessage("Cannot fire for another " + CrunchSiegeCore.convertTime(this.NextShotTime - System.currentTimeMillis()));
			return;
		}
		float loadedFuel = this.AmmoHolder.LoadedFuel;
		Material LoadedProjectile = this.AmmoHolder.MaterialName;


		LivingEntity living = (LivingEntity) Entity;
		if (living == null || living.getEquipment() == null || living.getEquipment().getHelmet() == null || living.getEquipment().getHelmet().getItemMeta() == null) {
			return;
		}
		
		if (living.getEquipment().getHelmet().getItemMeta().getCustomModelData() != this.ReadyModelNumber) {
			player.sendMessage("Cannot fire yet!");
			return;
		}
		this.AmmoHolder.LoadedFuel = 0;
		this.AmmoHolder.LoadedProjectile = 0;
		this.AmmoHolder.MaterialName = Material.BEDROCK;

		this.WorldName = Entity.getWorld().getName();
		this.NextShotTime = System.currentTimeMillis() + 1000;
		if (this.CycleThroughModelsBeforeFiring) {

	
			this.TaskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CrunchSiegeCore.plugin, () -> {

				if (this.HasFired) {
					Bukkit.getServer().getScheduler().cancelTask(this.TaskNumber);
					this.TaskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CrunchSiegeCore.plugin, () -> {

						//	player.sendMessage("task");
						if (this.HasReloaded) {
							Bukkit.getServer().getScheduler().cancelTask(this.TaskNumber);
							this.HasReloaded = false;
							this.HasFired = false;
							this.NextModelNumber = 0;
							CrunchSiegeCore.UpdateEntityIdModel(this.Entity, this.ReadyModelNumber, this.WorldName);
							this.TaskNumber = 0;
						}
						else {
							//firing stages
							if (this.NextModelNumber - 1 <= this.FiringModelNumbers.size() && this.NextModelNumber - 1 >= 0) {
								int modelData = this.FiringModelNumbers.get(this.NextModelNumber - 1);
								//	player.sendMessage("" + modelData);
								CrunchSiegeCore.UpdateEntityIdModel(this.Entity, modelData, this.WorldName);
								this.NextModelNumber -= 1;

							}else {
								//	plugin.getLogger().log(Level.INFO, "its reloaded");
								this.HasReloaded = true;
							}
						}
					}, 0, this.MillisecondsBetweenReloadingStages);

				}
				else {
					//firing stages
					if (this.NextModelNumber < this.FiringModelNumbers.size()) {

						int modelData = this.FiringModelNumbers.get(this.NextModelNumber);
						//	player.sendMessage("" + modelData);
						CrunchSiegeCore.UpdateEntityIdModel(this.Entity, modelData, this.WorldName);
						if (modelData == this.ModelNumberToFireAt) {
						//	player.sendMessage("firing" + modelData);
							Projectiles.get(LoadedProjectile).Shoot(player, this.Entity, this.GetFireLocation(living),  loadedFuel * this.VelocityPerFuel);
						}
						this.NextModelNumber += 1;

					}else {
						this.HasFired = true;

					}
				}
			}, 0, this.MillisecondsBetweenFiringStages);
		}
		else {

			this.TaskNumber = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CrunchSiegeCore.plugin, () -> {
				//player.sendMessage("task");

				Location loc = living.getEyeLocation();
				Vector direction = Entity.getLocation().getDirection().multiply(XOffset);

				loc.add(direction);

				this.NextModelNumber = 0;

				Projectiles.get(LoadedProjectile).Shoot(player, this.Entity, this.GetFireLocation(living), loadedFuel * this.VelocityPerFuel);
			}, (long) delay);

		}
	}


}
