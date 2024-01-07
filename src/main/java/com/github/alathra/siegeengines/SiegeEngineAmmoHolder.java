package com.github.alathra.siegeengines;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SiegeEngineAmmoHolder {
	
	public int loadedFuel = 0;
    public int loadedProjectile = 0;
    public ItemStack materialName = new ItemStack(Material.AIR, 1);
	
	public SiegeEngineAmmoHolder() {
		loadedFuel = 0;
		loadedProjectile = 0;
		materialName = new ItemStack(Material.AIR, 1);
	}
}
