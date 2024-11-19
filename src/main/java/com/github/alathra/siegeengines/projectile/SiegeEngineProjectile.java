package com.github.alathra.siegeengines.projectile;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class SiegeEngineProjectile {

    protected final ProjectileType projectileType;
    protected final ItemStack ammunitionItem;

    public SiegeEngineProjectile(ProjectileType projectileType, @NotNull ItemStack ammunitionItem) {
        this.projectileType = projectileType;
        this.ammunitionItem = ammunitionItem;
    }

    public abstract void Shoot(Entity player, Entity entity, Location fireLocation, Float velocity);

    public ProjectileType getType() {
        return projectileType;
    }

    public ItemStack getAmmuinitionItem() {
        return ammunitionItem;
    }

}

