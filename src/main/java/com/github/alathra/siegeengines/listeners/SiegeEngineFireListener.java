package com.github.alathra.siegeengines.listeners;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.github.alathra.siegeengines.SiegeEngine;
import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.util.SiegeEnginesUtil;

public class SiegeEngineFireListener implements Listener {
	
	@EventHandler
	public void onSiegeEngineFire(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack ItemInHand = event.getPlayer().getInventory().getItemInMainHand();
		if (ItemInHand == null) {
			return;
		}

		if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			if (ItemInHand.getType() != Config.controlItem) {
				return;
			}
			// If player is not sneaking, fire SiegeEngine
			if (!player.isSneaking()) {
				List<Entity> siegeEngineEntities = SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId());
				if (siegeEngineEntities == null) {
					return;
				}
				for (Entity siegeEngineEntity : siegeEngineEntities) {
					if (SiegeEngines.activeSiegeEngines.containsKey(siegeEngineEntity.getUniqueId())) {
						SiegeEngine siegeEngine = SiegeEngines.activeSiegeEngines.get(siegeEngineEntity.getUniqueId());
						if (siegeEngine != null && siegeEngine.getEnabled() && !(siegeEngineEntity.isDead())
								&& siegeEngine.isLoaded()) {
							if (siegeEngine.isSetModelNumberWhenFullyLoaded() && siegeEngine.canLoadFuel()) {
								player.sendMessage("§eFailed to fire. This siege engine needs to be fully loaded!");
							} else {
								siegeEngine.Fire(player, 10f, 1);
							}
						}
					}
				}
				return;
			}
		}
		
		// Attempt auto-reload if enabled in config
		if (Config.autoReload) {
			SiegeEnginesUtil.autoReload(player);
		}
		
	}
}
