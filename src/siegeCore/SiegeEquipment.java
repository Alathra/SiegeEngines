package siegeCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SiegeEquipment implements Cloneable  {
	
	@Override
	public SiegeEquipment clone() throws CloneNotSupportedException {
	return (SiegeEquipment)super.clone();
	}
	
	public UUID EntityId;
	public Entity Entity;
	
	public ItemStack ItemToPlace;
	
	public String ItemToUseForModel;
	
	public String WorldName;
	
	public int FuseTime;
	public double Velocity = 3;
	public long NextShotTime = System.currentTimeMillis();
	
	public int MillisecondsBetweenFiringStages;
	public int MillisecondsBetweenReloadingStages;
	
	public int MillisecondsToLoad;
	public Location location;
	public int ModelNumberToFireAt;
	public Boolean CycleThroughModelsBeforeFiring = false;
	public Boolean RotateSideways = true;
    public Boolean RotateUpDown = true;
	
	public Boolean HasFired = false;
	public Boolean HasReloaded = false;
	
	public int TaskNumber;
	
	public int ReadyModelNumber;
	
	public int NextModelNumber = 0;
	
	public List<Integer> FiringModelNumbers = new ArrayList<Integer>();
	
	public SiegeProjectile projectile = new SiegeProjectile(3, false);
	
	public SiegeEquipment(UUID id) {
		EntityId = id;
	}
	
	public void Fire(Player player) {
		LivingEntity living = (LivingEntity) Entity;
		Location loc = living.getEyeLocation();
		Random random = new Random();

		float randomVar = random.nextFloat() * (7 - -7) + -7;

		this.NextModelNumber = 0;
		this.location = loc;
		this.NextShotTime = System.currentTimeMillis() + 6000;
		player.sendMessage("Cannot fire for another " + CrunchSiegeCore.convertTime(this.NextShotTime - System.currentTimeMillis()));
		this.WorldName = Entity.getWorld().getName();
		Bukkit.getServer().getWorld(this.WorldName).playSound(this.location, Sound.ENTITY_BAT_DEATH, 20, 2);
		if (this.CycleThroughModelsBeforeFiring) {
			

			this.TaskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CrunchSiegeCore.plugin, () -> {
			//player.sendMessage("task");
			if (this.HasFired) {
				Bukkit.getServer().getScheduler().cancelTask(this.TaskNumber);
				this.TaskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CrunchSiegeCore.plugin, () -> {
					//	player.sendMessage("task");
					if (this.HasReloaded) {
						Bukkit.getServer().getScheduler().cancelTask(this.TaskNumber);
						this.HasReloaded = false;
						this.HasFired = false;
						this.NextModelNumber = 0;
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
						Entity tnt = Bukkit.getServer().getWorld(this.WorldName).spawnEntity(loc, EntityType.SNOWBALL);

						ClickHandler.projectiles.put(tnt.getUniqueId(), this.projectile);
						tnt.setVelocity(loc.getDirection().multiply(this.Velocity));

					}
					this.NextModelNumber += 1;

				}else {
					this.HasFired = true;

				}
			}
		}, 0, this.MillisecondsBetweenFiringStages);
		}
		else {
			Entity tnt = Bukkit.getServer().getWorld(this.WorldName).spawnEntity(loc, EntityType.SNOWBALL);

			ClickHandler.projectiles.put(tnt.getUniqueId(), this.projectile);
			tnt.setVelocity(loc.getDirection().multiply(this.Velocity));
		}
	}
	
}
