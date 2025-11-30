package io.github.alathra.siegeengines.core.shot;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public interface ShotAction {
    void onPreFire();
    void onFire(List<Entity> projectiles);
    void onCollideWithEntity(Location location, Entity projectile, LivingEntity hitEntity);
    void onCollideWithBlock(Location location, Entity projectile, Block hitBlock);
}
