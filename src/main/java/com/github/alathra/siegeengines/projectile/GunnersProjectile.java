package com.github.alathra.siegeengines.projectile;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface GunnersProjectile {
    public void Shoot(Entity player, Entity entity, Location fireLocation, Float velocity);

    public String ProjectileType = "Default";
}

