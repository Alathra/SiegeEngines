package com.github.alathra.siegeengines.listeners;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.github.alathra.siegeengines.SiegeEngine;
import com.github.alathra.siegeengines.SiegeEngines;

public class SiegeEngineDeathListener implements Listener {
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSiegeEngineDeathEvent(EntityDeathEvent event) {
		
		boolean removeStands = false;
		List<ItemStack> items = event.getDrops();
		if (event.getEntity() instanceof ArmorStand) {
			// Namespace key
			NamespacedKey key = new NamespacedKey(SiegeEngines.getInstance(), "siege_engines");
			if (event.getEntity().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
				Entity base = Bukkit.getEntity(UUID.fromString(
						event.getEntity().getPersistentDataContainer().get(key, PersistentDataType.STRING)));
				base.remove();
			}
			if (SiegeEngines.activeSiegeEngines.containsKey(event.getEntity().getUniqueId())) {
				removeStands = true;
			} else {
				for (ItemStack i : items) {
					if (i.getType() == Material.CARVED_PUMPKIN && i.hasItemMeta()
							&& i.getItemMeta().hasCustomModelData()) {
						// If siege engine is default model number
						if (SiegeEngines.definedSiegeEngines.containsKey(i.getItemMeta().getCustomModelData())) {
							removeStands = true;
							break;
						}
						// If siege engine is some other model number because it was broken in the middle of firing
						for (SiegeEngine siegeEngine : SiegeEngines.definedSiegeEngines.values()) {
							if (siegeEngine.getFiringModelNumbers().contains(i.getItemMeta().getCustomModelData())) {
								removeStands = true;
								break;
							}
						}
					}
				}
			}
			if (removeStands) {
				for (ItemStack i : items) {
					if (i.getType() == Material.ARMOR_STAND) {
						PlayerHandler.siegeEngineEntityDied(event.getEntity(),false);
						SiegeEngines.activeSiegeEngines.remove(event.getEntity().getUniqueId());
						i.setAmount(0);
						return;
					}
				}
			}
		}
	}
}
