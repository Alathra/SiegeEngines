package io.github.alathra.siegeengines.core.shot.attribute;

import net.kyori.adventure.util.TriState;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class BurningShotAttribute extends ShotAttribute {

    private final int projectileBurnSeconds;
    private final int entityBurnSeconds;

    public BurningShotAttribute(int projectileBurnSeconds, int entityBurnSeconds) {
        super(AttributeType.BURNING);
        this.projectileBurnSeconds = projectileBurnSeconds;
        this.entityBurnSeconds = entityBurnSeconds;
    }

    public int getProjectileBurnSeconds() {
        return projectileBurnSeconds;
    }

    public int getEntityBurnSeconds() {
        return entityBurnSeconds;
    }

    @Override
    public void onPreFire() {
    }

    @Override
    public void onFire(List<Entity> projectiles) {
        projectiles.forEach(projectile -> {
            projectile.setVisualFire(TriState.TRUE);
            projectile.setFireTicks(entityBurnSeconds*20);
        });
    }

    @Override
    public void onCollideWithEntity(Location location, Entity projectile, LivingEntity hitEntity) {
        hitEntity.setFireTicks(entityBurnSeconds*20);
    }

    @Override
    public void onCollideWithBlock(Location location, Entity projectile, Block hitBlock) {

    }

    public static class Builder {

        // DEFAULTS
        private int projectileBurnSeconds = 30;
        private int entityBurnSeconds = 10;

        public void setProjectileBurnSeconds(int projectileBurnSeconds) {
            this.projectileBurnSeconds = projectileBurnSeconds;
        }

        public void setEntityBurnSeconds(int entityBurnSeconds) {
            this.entityBurnSeconds = entityBurnSeconds;
        }

        public BurningShotAttribute build() {
            return new BurningShotAttribute(
                projectileBurnSeconds,
                entityBurnSeconds
            );
        }
    }
}
