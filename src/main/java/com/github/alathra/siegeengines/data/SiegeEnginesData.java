package com.github.alathra.siegeengines.data;

import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.projectile.ExplosiveProjectile;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;

public class SiegeEnginesData {
    public static HashMap<UUID, ExplosiveProjectile> projectiles;
    public static EnumSet<Material> fluidMaterials;
    public static NamespacedKey key;
    public static ArrayList<ItemStack> items;

    public static void init() {
        projectiles = new HashMap<>();
        fluidMaterials = EnumSet.of(Material.WATER, Material.LAVA, Material.BUBBLE_COLUMN,
            Material.SEAGRASS, Material.TALL_SEAGRASS, Material.KELP, Material.KELP_PLANT, Material.SEA_PICKLE);
        key = new NamespacedKey(SiegeEngines.getInstance(), "siege_engines");
        items = new ArrayList<>();
    }
}
