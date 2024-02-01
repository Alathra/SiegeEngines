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
        siegeEngineEntityDied(entity,true);
    }
    public static void siegeEngineEntityDied(Entity entity, boolean silent) {
        for (UUID uuid : SiegeEngines.siegeEngineEntitiesPerPlayer.keySet()) {
            if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(uuid)) {
                for (UUID eUuid : SiegeEngines.activeSiegeEngines.keySet()) {
                    if (Bukkit.getEntity(eUuid) == null || Bukkit.getEntity(eUuid).isDead() || !Bukkit.getEntity(eUuid).isValid()) {
                        SiegeEngines.activeSiegeEngines.remove(eUuid);
                        if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid) != null) SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(Bukkit.getEntity(eUuid));
                    }
                }
				SiegeEngines.activeSiegeEngines.remove(entity.getUniqueId());
                SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(entity);
                SiegeEnginesLogger.debug("ENGINE DISABLED/DEAD "+entity.getUniqueId().toString());
            }
            if (!silent) Bukkit.getPlayer(uuid).sendMessage("§eSiege Engine Destroyed!");
            for (UUID eUuid : SiegeEngines.activeSiegeEngines.keySet()) {
                if (Bukkit.getEntity(eUuid) == null || Bukkit.getEntity(eUuid).isDead() || !Bukkit.getEntity(eUuid).isValid()) {
                    SiegeEngines.activeSiegeEngines.remove(eUuid);
                    if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid) != null) SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(Bukkit.getEntity(eUuid));
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
            if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid) != null) SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).clear();
            for (UUID eUuid : SiegeEngines.activeSiegeEngines.keySet()) {
                if (Bukkit.getEntity(eUuid) == null || Bukkit.getEntity(eUuid).isDead() || !Bukkit.getEntity(eUuid).isValid()) {
                    SiegeEngines.activeSiegeEngines.remove(eUuid);
                    if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid) != null) SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(Bukkit.getEntity(eUuid));
                }
            }
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
                for (UUID eUuid : SiegeEngines.activeSiegeEngines.keySet()) {
                    if (Bukkit.getEntity(eUuid) == null || Bukkit.getEntity(eUuid).isDead() || !Bukkit.getEntity(eUuid).isValid()) {
                        SiegeEngines.activeSiegeEngines.remove(eUuid);
                        if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid) != null) SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(Bukkit.getEntity(eUuid));
                    }
                }
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
                    SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(entity);
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
                SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).remove(entity);
                player.sendMessage("§eReleased this SiegeEngine!");
                return;
            }
            if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid) != null) {
                SiegeEnginesLogger.debug("LEFT-OVER ENGINES? "+SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).toString());
            }
            list.clear();
            player.sendMessage("§eReleased all Siege Engines!");
            SiegeEngines.siegeEngineEntitiesPerPlayer.put(uuid,list); 
            SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).clear();
            SiegeEngines.siegeEngineEntitiesPerPlayer.remove(uuid);
        }
    }
}
