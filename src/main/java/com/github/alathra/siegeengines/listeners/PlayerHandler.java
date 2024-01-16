package com.github.alathra.siegeengines.listeners;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import com.github.alathra.siegeengines.SiegeEngines;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.alathra.siegeengines.SiegeEngine;
import com.github.alathra.siegeengines.config.Config;

public class PlayerHandler implements Listener {

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        PlayerJoinLeave(event.getPlayer());
    }

    @EventHandler
    public void playerLeaveEvent(PlayerQuitEvent event) {
        PlayerJoinLeave(event.getPlayer());
    }
    @EventHandler
    public void playerDeathEvent(PlayerDeathEvent event) {
        PlayerJoinLeave(event.getPlayer());
    }
    @EventHandler
    public void entityDeathEvent(EntityDeathEvent event) {
        SiegeEngineEntityDied(event.getEntity());
    }
    public void SiegeEngineEntityDied(Entity entity) {
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
    public void PlayerJoinLeave(Player player) {
        if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
            final List<Entity> list = new ArrayList<>(SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()));
            list.clear();
            SiegeEngines.siegeEngineEntitiesPerPlayer.put(player.getUniqueId(),list);
        }
    }
}
