package io.github.alathra.siegeengines.core.shot;

import io.github.alathra.siegeengines.core.shot.attribute.ShotAttribute;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.List;

public abstract class Shot {

    private final Material ammunition;
    private final int ammunitionAmount;
    private final List<ShotAttribute> attributes;
    private final EntityType baseEntityType;

    protected Shot(Material ammunition, int ammunitionAmount, EntityType baseEntityType) {
        this.ammunition = ammunition;
        this.ammunitionAmount = ammunitionAmount;
        this.baseEntityType = baseEntityType;
        this.attributes = Collections.emptyList();
    }

    protected Shot(Material ammunition, int ammunitionAmount, EntityType baseEntityType, List<ShotAttribute> attributes) {
        this.ammunition = ammunition;
        this.ammunitionAmount = ammunitionAmount;
        this.attributes = attributes;
        this.baseEntityType = baseEntityType;
    }
}
