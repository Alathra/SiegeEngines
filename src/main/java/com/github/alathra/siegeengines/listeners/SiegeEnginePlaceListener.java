package com.github.alathra.siegeengines.listeners;

import com.github.alathra.siegeengines.SiegeEngine;
import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.data.SiegeEnginesData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class SiegeEnginePlaceListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSiegeEnginePlace(org.bukkit.event.block.BlockPlaceEvent event) {
        Player thePlayer = event.getPlayer();
        Material replaced = event.getBlockReplacedState().getType();
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.CARVED_PUMPKIN) {
            ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            if (item.getItemMeta() != null && item.getItemMeta().hasCustomModelData()) {
                int customModel = item.getItemMeta().getCustomModelData();
                SiegeEngine siegeEngine = null;
                // Search for match in custom model data value in defined siege engines
                for (SiegeEngine entry : SiegeEngines.definedSiegeEngines.values()) {
                    if (entry.getCustomModelID() == customModel) {
                        try {
                            siegeEngine = entry.clone();
                        } catch (CloneNotSupportedException e) {
                            break;
                        }
                    } else {
                        // if siege engine was broken during one of its firing stages
                        if (entry.getFiringModelNumbers().contains(customModel)) {
                            try {
                                siegeEngine = entry.clone();
                            } catch (CloneNotSupportedException e) {
                                break;
                            }
                        }
                    }
                }
                // If SiegeEngine found, place it
                if (siegeEngine != null) {
                    if (Config.disabledWorlds.contains(thePlayer.getWorld())) {
                        thePlayer.sendMessage("§eSiege Engines cannot be placed in this World.");
                        event.setCancelled(true);
                    }
                    if (SiegeEnginesData.fluidMaterials.contains(replaced)) {
                        thePlayer.sendMessage("§eSiege Engines cannot be placed in Fluid Blocks.");
                        event.setCancelled(true);
                    }
                    if (event.isCancelled()) {
                        return;
                    }
                    if (siegeEngine.place(thePlayer, event.getBlockAgainst().getLocation())) {
                        item.setAmount(item.getAmount() - 1);
                        thePlayer.getInventory().setItemInMainHand(item);
                        thePlayer.sendMessage("§eSiege Engine placed!");
                    } else {
                        thePlayer.sendMessage(
                            "§eSiege Engine cannot be placed within a " + Config.placementDensity + " Block-Radius of other Siege Engines.");
                    }
                    event.setCancelled(true);
                }
            }
        } else if (event.getPlayer().getInventory().getItemInOffHand().getType() == Material.CARVED_PUMPKIN) {
            ItemStack item = event.getPlayer().getInventory().getItemInOffHand();
            if (item.getItemMeta() != null && item.getItemMeta().hasCustomModelData()) {
                int customModel = item.getItemMeta().getCustomModelData();
                SiegeEngine siegeEngine = null;
                // Search for match in custom model data value in defined siege engines
                for (SiegeEngine entry : SiegeEngines.definedSiegeEngines.values()) {
                    if (entry.getCustomModelID() == customModel) {
                        try {
                            siegeEngine = entry.clone();
                        } catch (CloneNotSupportedException e) {
                            break;
                        }
                    } else {
                        // if siege engine was broken during one of its firing stages
                        if (entry.getFiringModelNumbers().contains(customModel)) {
                            try {
                                siegeEngine = entry.clone();
                            } catch (CloneNotSupportedException e) {
                                break;
                            }
                        }
                    }
                }
                // If SiegeEngine found, place it
                if (siegeEngine != null) {
                    if (Config.disabledWorlds.contains(thePlayer.getWorld())) {
                        thePlayer.sendMessage("§eSiege Engines cannot be placed in this World.");
                        event.setCancelled(true);
                    }
                    if (SiegeEnginesData.fluidMaterials.contains(replaced)) {
                        thePlayer.sendMessage("§eSiege Engines cannot be placed in Fluid Blocks.");
                        event.setCancelled(true);
                    }
                    if (event.isCancelled()) {
                        return;
                    }
                    if (siegeEngine.place(thePlayer, event.getBlockAgainst().getLocation())) {
                        item.setAmount(item.getAmount() - 1);
                        thePlayer.getInventory().setItemInOffHand(item);
                        thePlayer.sendMessage("§eSiege Engine placed!");
                    } else {
                        thePlayer.sendMessage(
                            "§eSiege Engine cannot be placed within a " + Config.placementDensity + " Block-Radius of other Siege Engines.");
                    }
                    event.setCancelled(true);
                }
            }
        }
    }
}
