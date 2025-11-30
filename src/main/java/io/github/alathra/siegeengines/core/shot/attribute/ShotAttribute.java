package io.github.alathra.siegeengines.core.shot.attribute;

import io.github.alathra.siegeengines.core.shot.ShotAction;

public abstract class ShotAttribute implements ShotAction {

    private final AttributeType attributeType;

    public ShotAttribute(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public AttributeType getType() {
        return attributeType;
    }

    public enum AttributeType {
        BURNING,
        CUSTOM_MODEL,
        DAMAGING,
        EXPLODING,
        FIREWORK,
        LINGERING_POTION,
        MULTISHOT,
        POTION,
    }
}
