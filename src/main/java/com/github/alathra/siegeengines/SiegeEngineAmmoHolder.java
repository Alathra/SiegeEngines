package com.github.alathra.siegeengines;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SiegeEngineAmmoHolder {
	
	private int loadedFuel = 0;
    private int loadedProjectile = 0;
    private ItemStack materialName = new ItemStack(Material.AIR, 1);
	
	public SiegeEngineAmmoHolder() {
		setLoadedFuel(0);
		setLoadedProjectile(0);
		setMaterialName(new ItemStack(Material.AIR, 1));
	}

	public int getLoadedFuel() {
		return loadedFuel;
	}

	public void setLoadedFuel(int loadedFuel) {
		this.loadedFuel = loadedFuel;
	}

	public int getLoadedProjectile() {
		return loadedProjectile;
	}

	public void setLoadedProjectile(int loadedProjectile) {
		this.loadedProjectile = loadedProjectile;
	}

	public ItemStack getMaterialName() {
		return materialName;
	}

	public void setMaterialName(ItemStack materialName) {
		this.materialName = materialName;
	}
}
