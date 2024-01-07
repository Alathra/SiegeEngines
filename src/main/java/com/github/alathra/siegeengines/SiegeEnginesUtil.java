package com.github.alathra.siegeengines;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class SiegeEnginesUtil {
	
	public static void UpdateEntityIdModel(Entity ent, int modelNumber, String WorldName) {
        if (ent instanceof LivingEntity) {
            LivingEntity liv = (LivingEntity) ent;
            ItemStack Helmet = liv.getEquipment().getHelmet();
            if (Helmet != null) {
                ItemMeta meta = Helmet.getItemMeta();
                meta.setCustomModelData(modelNumber);
                Helmet.setItemMeta(meta);
                liv.getEquipment().setHelmet(Helmet);
            }
        }
    }
	
	public static FixedMetadataValue addMetaDataValue(Object value) {
        return new FixedMetadataValue(Bukkit.getServer().getPluginManager().getPlugin("SiegeEngines"), value);
    }
	
	public static String convertTime(long time) {
        long days = TimeUnit.MILLISECONDS.toDays(time);
        time -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(time);
        time -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
        String timeLeftFormatted = String.format("§e" + minutes + " Minutes " + seconds + " Seconds§f");
        return timeLeftFormatted;
    }
	
	public static SiegeEngine createCloneFromCustomModelData(Integer ModelId) {
        try {
            return SiegeEngines.definedSiegeEngines.get(ModelId).clone();
        } catch (CloneNotSupportedException e) {

        }
        return null;
    }
	
	public static boolean hasItem(Inventory inv, ItemStack m) {
		return inv.containsAtLeast(m, m.getAmount());
	}
}
