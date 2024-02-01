package com.github.alathra.siegeengines.listeners;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import com.github.alathra.siegeengines.SiegeEngines;
import com.github.alathra.siegeengines.SiegeEnginesLogger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class PlayerHandler implements Listener {

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        playerJoinLeave(event.getPlayer());
    }

    @EventHandler
    public void playerLeaveEvent(PlayerQuitEvent event) {
        playerJoinLeave(event.getPlayer());
    }
    @EventHandler
    public void playerDeathEvent(PlayerDeathEvent event) {
        playerJoinLeave(event.getPlayer());
    }
    public static void siegeEngineEntityDied(Entity entity) {
        for (UUID uuid : SiegeEngines.siegeEngineEntitiesPerPlayer.keySet()) {
            if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(uuid)) {
                final List<Entity> list = new ArrayList<>(SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid));
                if (list.contains(entity.getUniqueId())) {
                    list.remove(entity);
                    Bukkit.getPlayer(uuid).sendMessage("§eSiegeEngine Destroyed!");
                    SiegeEngines.siegeEngineEntitiesPerPlayer.put(uuid,list);
                    SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(entity);
                }
                SiegeEnginesLogger.debug("ENGINE DISABLED/DEAD "+entity.getUniqueId().toString());
                if (entity.isDead()) {
                    SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(entity);
                    SiegeEngines.activeSiegeEngines.remove(entity.getUniqueId());
                }
                if (!entity.isValid()) {
                    SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(entity);
                    SiegeEngines.activeSiegeEngines.remove(entity.getUniqueId());
                }
            }
        }
    }
    public static void playerJoinLeave(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        SiegeEnginesLogger.debug("CLEARING PLAYER-ENGINES "+player.getName());
        if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(uuid)) {
            final List<Entity> list = new ArrayList<>(SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid));
            list.clear();
            SiegeEngines.siegeEngineEntitiesPerPlayer.put(uuid,list);
            SiegeEngines.siegeEngineEntitiesPerPlayer.remove(uuid);
            SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).clear();
        }
        if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid) != null) {
            SiegeEnginesLogger.debug("LEFT-OVER ENGINES? "+SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).toString());
        }
    }
    public static void releasePlayerSiegeEngine(Player player, Entity entity) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(uuid)) {
            final List<Entity> list = new ArrayList<>(SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid));
            if (entity != null) {
                if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).contains(entity.getUniqueId())) {
                    list.remove(entity);
                    if (entity.isDead()) {
                        SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(entity);
                        SiegeEngines.activeSiegeEngines.remove(entity.getUniqueId());
                    }
                    if (!entity.isValid()) {
                        SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(entity);
                        SiegeEngines.activeSiegeEngines.remove(entity.getUniqueId());
                    }
                    SiegeEngines.siegeEngineEntitiesPerPlayer.put(uuid,list);
                    SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(entity.getUniqueId());
                }
                SiegeEnginesLogger.debug("ENGINE RELEASED "+entity.getUniqueId().toString());
                if (entity.isDead()) {
                    SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(entity);
                    SiegeEngines.activeSiegeEngines.remove(entity.getUniqueId());
                }
                if (!entity.isValid()) {
                    SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(entity);
                    SiegeEngines.activeSiegeEngines.remove(entity.getUniqueId());
                }
                player.sendMessage("§eReleased this SiegeEngine!");
                return;
            }
            if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid) != null) {
                SiegeEnginesLogger.debug("LEFT-OVER ENGINES? "+SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).toString());
            }
            list.clear();
            player.sendMessage("§eReleased all SiegeEngines!");
            SiegeEngines.siegeEngineEntitiesPerPlayer.put(uuid,list); 
            SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).clear();
            SiegeEngines.siegeEngineEntitiesPerPlayer.remove(uuid);
        }
    }
}
