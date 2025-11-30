package io.github.alathra.siegeengines.core.shot.attribute;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TNTPrimed;

import java.util.List;

public class ExplodingShotAttribute extends ShotAttribute {

    private final float explosionPower;
    private final boolean explodeOnEntityCollide;

    public ExplodingShotAttribute(float explosionPower, boolean explodeOnEntityCollide) {
        super(AttributeType.EXPLODING);
        this.explosionPower = explosionPower;
        this.explodeOnEntityCollide = explodeOnEntityCollide;
    }

    private void createExplosion(Location location) {
        TNTPrimed explosion = (TNTPrimed) location.getWorld().spawnEntity(location, EntityType.TNT);
        explosion.setYield(explosionPower);
        explosion.setFireTicks(0);
    }

    @Override
    public void onPreFire() {
    }

    @Override
    public void onFire(List<Entity> projectiles) {

    }

    @Override
    public void onCollideWithEntity(Location location, Entity projectile, LivingEntity hitEntity) {
        if (explodeOnEntityCollide) {
            createExplosion(location);
        }
    }

    @Override
    public void onCollideWithBlock(Location location, Entity projectile, Block hitBlock) {
        createExplosion(location);
    }

    public static class Builder {

        // DEFAULTS
        private float explosionPower = 3.0f;
        private boolean explodeOnEntityCollide = true;

        public void setExplosionPower(float explosionPower) {
            this.explosionPower = explosionPower;
        }

        public void setExplodeOnEntityCollide(boolean explodeOnEntityCollide) {
            this.explodeOnEntityCollide = explodeOnEntityCollide;
        }

        public ExplodingShotAttribute build() {
            return new ExplodingShotAttribute(
                explosionPower,
                explodeOnEntityCollide
            );
        }
    }
}
