package siegeCore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class SiegeEquipment {

	public UUID EntityId;
	public Entity Entity;
	
	public ItemStack ItemToPlace;
	
	public String ItemToUseForModel;
	
	public String WorldName;
	
	public int FuseTime;
	public double Velocity = 1;
	public long NextShotTime = System.currentTimeMillis();
	
	public int MillisecondsBetweenFiringStages;
	public int MillisecondsBetweenReloadingStages;
	
	public int MillisecondsToLoad;
	public Location location;
	public int ModelNumberToFireAt;
	
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
	
}
