package com.github.alathra.siegeengines;

import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SiegeEnginesDamageSource implements DamageSource {

    private Entity causingEntity;
    private Entity directEntity;
    private Entity siegeEngineEntity;

    public SiegeEnginesDamageSource(Entity causingEntity, Entity directEntity, Entity siegeEngineEntity) {
        this.causingEntity = causingEntity;
        this.directEntity = directEntity;
        this.siegeEngineEntity = siegeEngineEntity;
    }

    @Override
    public @NotNull DamageType getDamageType() {
        return DamageType.ARROW;
    }

    @Override
    public @Nullable Entity getCausingEntity() {
        return causingEntity;
    }

    @Override
    public @Nullable Entity getDirectEntity() {
        return directEntity;
    }

    @Override
    public @Nullable Location getDamageLocation() {
        return siegeEngineEntity.getLocation();
    }

    @Override
    public @Nullable Location getSourceLocation() {
        return causingEntity.getLocation();
    }

    @Override
    public boolean isIndirect() {
        return true;
    }

    @Override
    public float getFoodExhaustion() {
        return 0;
    }

    @Override
    public boolean scalesWithDifficulty() {
        return false;
    }
}
