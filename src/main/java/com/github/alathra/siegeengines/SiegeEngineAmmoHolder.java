package com.github.alathra.siegeengines;

import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.data.SiegeEnginesData;
import com.github.alathra.siegeengines.listeners.PlayerHandler;
import com.github.alathra.siegeengines.projectile.ExplosiveProjectile;
import com.github.alathra.siegeengines.util.GeneralUtil;
import com.github.alathra.siegeengines.util.SiegeEnginesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
