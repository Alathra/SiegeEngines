package siegeCore;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SiegeProjectile {

	public int Radius = 2;
	public Boolean DoFire = false;
	
	public SiegeProjectile(int rad, boolean fire) {
		Radius = rad;
		DoFire = fire;
	}
	
	public ItemStack AmmoItem = new ItemStack(Material.COBBLESTONE, 1);
}
