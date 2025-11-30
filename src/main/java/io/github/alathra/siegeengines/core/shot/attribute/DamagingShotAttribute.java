package io.github.alathra.siegeengines.core.shot.attribute;

import com.google.common.math.DoubleMath;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DamagingShotAttribute extends ShotAttribute {

    private final double minDamage;
    private final double maxDamage;

    public DamagingShotAttribute(double minDamage, double maxDamage) {
        super(AttributeType.DAMAGING);
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
    }

    public double getMinDamage() {
        return minDamage;
    }

    public double getMaxDamage() {
        return maxDamage;
    }

    public boolean isUsingDamageRange() {
        return !DoubleMath.fuzzyEquals(minDamage, maxDamage, 1e-9);
    }

    @Override
    public void onPreFire() {
    }

    @Override
    public void onFire(List<Entity> projectiles) {

    }

    @Override
    public void onCollideWithEntity(Location location, Entity projectile, LivingEntity hitEntity) {
        // TODO: cancel vanilla damage
        hitEntity.damage(ThreadLocalRandom.current().nextDouble(minDamage, maxDamage));
    }

    @Override
    public void onCollideWithBlock(Location location, Entity projectile, Block hitBlock) {

    }

    public static class Builder {

        // DEFAULTS
        private double minDamage = 10.0;
        private double maxDamage = 10.0;

        public void setMinDamage(double minDamage) {
            this.minDamage = minDamage;
        }

        public void setMaxDamage(double maxDamage) {
            this.maxDamage = maxDamage;
        }

        public void setDamage(double damage) {
            this.minDamage = damage;
            this.maxDamage = damage;
        }

        public DamagingShotAttribute build() {
            return new DamagingShotAttribute(
                minDamage,
                maxDamage
            );
        }
    }
}
