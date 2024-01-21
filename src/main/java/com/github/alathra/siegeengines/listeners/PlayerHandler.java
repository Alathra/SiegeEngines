package com.github.alathra.siegeengines.listeners;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import com.github.alathra.siegeengines.SiegeEngines;
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
    @EventHandler
    public void entityDeathEvent(EntityDeathEvent event) {
        siegeEngineEntityDied(event.getEntity());
    }
    public static void siegeEngineEntityDied(Entity entity) {
        for (UUID uuid : SiegeEngines.siegeEngineEntitiesPerPlayer.keySet()) {
            if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(uuid)) {
                final List<Entity> list = new ArrayList<>(SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid));
                if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).contains(entity.getUniqueId())) {
                    list.remove(entity);
                    SiegeEngines.siegeEngineEntitiesPerPlayer.put(uuid,list);
                }
            }
        }
    }
    public static void playerJoinLeave(Player player) {
        if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
            final List<Entity> list = new ArrayList<>(SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()));
            list.clear();
            SiegeEngines.siegeEngineEntitiesPerPlayer.put(player.getUniqueId(),list);
        }
    }
    public static void releasePlayerSiegeEngine(Player player, Entity entity) {
        UUID uuid = player.getUniqueId();
        if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(uuid)) {
            final List<Entity> list = new ArrayList<>(SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid));
            if (entity != null) {
                if (SiegeEngines.siegeEngineEntitiesPerPlayer.get(uuid).contains(entity.getUniqueId())) {
                    list.remove(entity);
                    SiegeEngines.siegeEngineEntitiesPerPlayer.put(uuid,list);
                    player.sendMessage("§eReleased this SiegeEngine!");
                }
                return;
            }
            list.clear();
            player.sendMessage("§eReleased all SiegeEngines!");
            SiegeEngines.siegeEngineEntitiesPerPlayer.put(uuid,list); 
        }
    }
}
