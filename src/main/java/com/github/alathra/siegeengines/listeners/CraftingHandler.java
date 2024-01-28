package com.github.alathra.siegeengines.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.Recipe;

import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.crafting.CraftingRecipes;

public class CraftingHandler implements Listener {

	@EventHandler
	public void onCraft(CraftItemEvent event) {
		if (!Config.craftingRecipes) {
			return;
		}
		Recipe recipe = event.getRecipe();
		if (!(event.getView().getPlayer() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getView().getPlayer();
		if (recipe.equals(CraftingRecipes.trebuchetRecipe())) {
			if (!player.hasPermission("SiegeEngines.craft.trebuchet")) {
				event.setCancelled(true);
			}
		} else if (recipe.equals(CraftingRecipes.ballistaRecipe())) {
			if (!player.hasPermission("SiegeEngines.craft.ballista")) {
				event.setCancelled(true);
			}
		} else if (recipe.equals(CraftingRecipes.swivelCannonRecipe())) {
			if (!player.hasPermission("SiegeEngines.craft.swivel_cannon")) {
				event.setCancelled(true);
			}
		} else if (recipe.equals(CraftingRecipes.breachCannonRecipe())) {
			if (!player.hasPermission("SiegeEngines.craft.breach_cannon")) {
				event.setCancelled(true);
			}
		}
	}

}